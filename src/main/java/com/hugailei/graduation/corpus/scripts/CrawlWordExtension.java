package com.hugailei.graduation.corpus.scripts;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

/**
 * @author HU Gailei
 * @date 2018/11/29
 * <p>
 * description: 爬取单词的扩展信息，包括同义词，近义词，反义词等
 * </p>
 **/
public class CrawlWordExtension {
    public static final String URL = "https://tuna.thesaurus.com/relatedWords/{1}?limit=9&offset=0";
    public static final String WORD_LIST_FILE_PATH = "E:\\毕业论文相关\\word\\allwords.txt";

    private static final String DB_HOST = "192.168.99.100";
    private static final String DB_PORT = "3307";
    private static final String DB_NAME="corpus";
    private static final String COLL_NAME = "tb_word_extension";
    private static final String USER_NAME="root";
    private static final String USER_PASSWORD="123456";

    public static void main(String[] args) throws Exception{
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("数据库连接成功！");
        }

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        FileReader fileReader = new FileReader(new File(WORD_LIST_FILE_PATH));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String word;
        while ((word = bufferedReader.readLine()) != null) {
            System.out.println("正在爬取单词：" + word);
            HttpGet httpGet = new HttpGet(URL.replace("{1}", word));

            CloseableHttpResponse response = null;
            int retryTime = 1;
            boolean failed = true;
            while (failed && retryTime <= 3) {
                try {
                    response = httpClient.execute(httpGet);
                    failed = false;
                } catch (IOException e) {
                    long waitTime = 10000 * retryTime;
                    System.out.println("网络超时，等待" + waitTime/1000 + "秒后重试第" + retryTime + "次:" + word);
                    Thread.sleep(waitTime);
                    retryTime ++;
                    if (retryTime > 3) {
                        throw e;
                    }
                }
            }
            String entityString = EntityUtils.toString(response.getEntity());
            JSONObject resultJson = (JSONObject) JSONObject.parse(entityString);
            if ("{\"data\":null}".equals(resultJson)) {
                continue;
            }
            JSONArray dataJson = resultJson.getJSONArray("data");
            Set<String> nounSynSet = new HashSet<>();
            Set<String> nounAntSet = new HashSet<>();
            Set<String> verbSynSet = new HashSet<>();
            Set<String> verbAntSet = new HashSet<>();
            Set<String> adjSynSet = new HashSet<>();
            Set<String> adjAntSet = new HashSet<>();
            Set<String> advSynSet = new HashSet<>();
            Set<String> advAntSet = new HashSet<>();

            if (dataJson != null) {
                for (int i=0; i<dataJson.size(); i++) {
                    JSONObject term = dataJson.getJSONObject(i);
                    String synWord = term.getString("targetTerm");
                    String pos = term.getString("pos");
                    switch (pos) {
                        case "noun" :
                            nounSynSet.add(synWord);
                            break;
                        case "verb" :
                            verbSynSet.add(synWord);
                            break;
                        case "adjective" :
                            adjSynSet.add(synWord);
                            break;
                        case "adverb" :
                            advSynSet.add(synWord);
                            break;
                        default:
                            break;
                    }

                    JSONArray synJsonArray = term.getJSONArray("synonyms");
                    JSONArray antJsonArray = term.getJSONArray("antonyms");

                    if (synJsonArray != null) {
                        for (int j=0; j<synJsonArray.size(); j++) {
                            JSONObject synJsonObject = synJsonArray.getJSONObject(j);
                            String synTerm = synJsonObject.getString("term");
                            switch (pos) {
                                case "noun" :
                                    nounSynSet.add(synTerm);
                                    break;
                                case "adjective" :
                                    adjSynSet.add(synTerm);
                                    break;
                                case "adverb" :
                                    advSynSet.add(synTerm);
                                    break;
                                case "verb" :
                                    verbSynSet.add(synTerm);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    if (antJsonArray != null) {
                        for (int j=0; j<antJsonArray.size(); j++) {
                            JSONObject antJsonObject = antJsonArray.getJSONObject(j);
                            String antTerm = antJsonObject.getString("term");
                            switch (pos) {
                                case "verb" :
                                    verbAntSet.add(antTerm);
                                    break;
                                case "noun" :
                                    nounAntSet.add(antTerm);
                                    break;
                                case "adjective" :
                                    adjAntSet.add(antTerm);
                                    break;
                                case "adverb" :
                                    advAntSet.add(antTerm);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } // for (int i=0; i<dataJson.size(); i++)

                // 存储到数据库
                System.out.println("准备存入数据库：" + word);
                if (!nounSynSet.isEmpty()) {
                    String resultString = "";
                    for (String result : nounSynSet) {
                        resultString = result + "," + resultString;
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "NN");
                    preparedStatement.setString(3, "syn");
                    preparedStatement.setString(4, resultString);
                    preparedStatement.execute();
                    System.out.println("名词性同义词存入完成");
                }

                if (!nounAntSet.isEmpty()) {
                    String resultString = "";
                    for (String result : nounAntSet) {
                        resultString = result + "," + resultString;
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "NN");
                    preparedStatement.setString(3, "ant");
                    preparedStatement.setString(4, resultString);
                    preparedStatement.execute();
                    System.out.println("名词性反义词存入完成：" + word);
                }

                if (!verbSynSet.isEmpty()) {
                    String resultString = "";
                    for (String result : verbSynSet) {
                        resultString = result + "," + resultString;
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "VB");
                    preparedStatement.setString(3, "syn");
                    preparedStatement.setString(4, resultString);
                    preparedStatement.execute();
                    System.out.println("动词性同义词存入完成：" + word);
                }

                if (!verbAntSet.isEmpty()) {
                    String resultString = "";
                    for (String result : verbAntSet) {
                        resultString = result + "," + resultString;
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "VB");
                    preparedStatement.setString(3, "ant");
                    preparedStatement.setString(4, resultString);
                    preparedStatement.execute();
                    System.out.println("动词性反义词存入完成：" + word);
                }

                if (!adjSynSet.isEmpty()) {
                    String resultString = "";
                    for (String result : adjSynSet) {
                        resultString = result + "," + resultString;
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "JJ");
                    preparedStatement.setString(3, "syn");
                    preparedStatement.setString(4, resultString);
                    preparedStatement.execute();
                    System.out.println("形容词性同义词存入完成：" + word);
                }

                if (!adjAntSet.isEmpty()) {
                    String resultString = "";
                    for (String result : adjAntSet) {
                        resultString = result + "," + resultString;
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "JJ");
                    preparedStatement.setString(3, "ant");
                    preparedStatement.setString(4, resultString);
                    preparedStatement.execute();
                    System.out.println("形容词性反义词存入完成：" + word);
                }

                if (!advSynSet.isEmpty()) {
                    String resultString = "";
                    for (String result : advSynSet) {
                        resultString = result + "," + resultString;
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "RB");
                    preparedStatement.setString(3, "syn");
                    preparedStatement.setString(4, resultString);
                    preparedStatement.execute();
                    System.out.println("副词性同义词存入完成：" + word);
                }

                if (!advAntSet.isEmpty()) {
                    String resultString = "";
                    for (String result : advAntSet) {
                        resultString = result + "," + resultString;
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "RB");
                    preparedStatement.setString(3, "ant");
                    preparedStatement.setString(4, resultString);
                    preparedStatement.execute();
                    System.out.println("副词性反义词存入完成：" + word);
                }

                System.out.println("存入完成:" + word);
            } //  if (dataJson != null)
        } // while ((word = bufferedReader.readLine()) != null) {
    }
}
