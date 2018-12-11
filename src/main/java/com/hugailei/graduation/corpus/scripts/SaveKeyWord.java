package com.hugailei.graduation.corpus.scripts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HU Gailei
 * @date 2018/12/10
 * <p>
 * description: 关键词计算及存储.
 * </p>
 **/
public class SaveKeyWord {

    private static String[] CORPUS_ARRAY = {"chinadaily","bnc"};

    private static String[] REF_CORPUS_ARRAY = {"chinadaily","bnc"};

    public static final double MIN_VALUE = 5.0 * Math.pow(Math.E, -324);

    private static final String DB_HOST = "192.168.99.100";

    private static final String DB_PORT = "3307";

    private static final String DB_NAME="corpus";

    private static final String USER_NAME="root";

    private static final String USER_PASSWORD="123456";
    
    public static void main(String[] args) throws Exception {

        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }
        
        for (String corpusName : CORPUS_ARRAY){
            for (String refCorpusName : REF_CORPUS_ARRAY) {
                if (!corpusName.equals( refCorpusName )) {
                    System.out.println("参考语料库：" + refCorpusName + "，观察语料库：" + corpusName);
                    // 获取语料库和参考语料库的总单词数
                    PreparedStatement preparedStatement = con.prepareStatement("SELECT SUM(freq) as totalWordNum FROM tb_word where corpus = '" + corpusName + "'");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    int corpusWordNum = 0;
                    if (resultSet.next()) {
                        corpusWordNum = resultSet.getInt("totalWordNum");
                    }
                    preparedStatement = con.prepareStatement("SELECT SUM(freq) as totalWordNum FROM tb_word where corpus = '" + refCorpusName + "'");
                    resultSet = preparedStatement.executeQuery();
                    int refCorpusWordNum = 0;
                    if (resultSet.next()) {
                        refCorpusWordNum = resultSet.getInt("totalWordNum");
                    }

                    // 查询参考语料库，存储对应的单词及频率
                    Map<String, Integer> refCorpusKey2Freq = new HashMap<>();
                    preparedStatement = con.prepareStatement("SELECT * FROM tb_word where corpus = '" + corpusName + "'");
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String form = resultSet.getString("form");
                        String pos = resultSet.getString("pos");
                        String lemma = resultSet.getString("lemma");
                        int freq = resultSet.getInt("freq");
                        String key = form + "_" + pos + "_" + lemma;
                        if (refCorpusKey2Freq.containsKey(key)) {
                            freq = refCorpusKey2Freq.get(key) + freq;
                        }
                        refCorpusKey2Freq.put(key, freq);
                    }

                    Map<String, Double> keyWordKey2Keyness = new HashMap<>();
                    preparedStatement = con.prepareStatement("SELECT * FROM tb_word where corpus = '" + corpusName + "'");
                    resultSet = preparedStatement.executeQuery();
                    //获取观察语料库和参考语料库的总词数
                    while (resultSet.next()) {
                        String form = resultSet.getString("form");
                        String pos = resultSet.getString("pos");
                        String lemma = resultSet.getString("lemma");
                        int freq = resultSet.getInt("freq");
                        String key = form + "_" + pos + "_" + lemma;
                        int refFreq = refCorpusKey2Freq.containsKey(key) ? refCorpusKey2Freq.get(key) : 0;
                        double keyness = getLogLikeliHoodValue(freq, refFreq, corpusWordNum, refCorpusWordNum);
                        keyWordKey2Keyness.put(key, keyness);
                    }
                    //在keywords表中插入记录
                    System.out.println("分析完成，开始插入数据库");
                    for (Map.Entry entry : keyWordKey2Keyness.entrySet()) {
                        String key = (String)entry.getKey();
                        double keyness = (double)entry.getValue();
                        System.out.println(key + ":" + keyness);
                        String word = key.split("_")[0];
                        String pos = key.split("_")[1];
                        String lemma = key.split("_")[2];
                        preparedStatement = con.prepareStatement("INSERT INTO tb_keyword"
                                + "(word, pos, lemma, corpus, ref_corpus, keyness) "
                                + "VALUES (?, ?, ?, ?, ?, ?)");
                        preparedStatement.setString(1, word);
                        preparedStatement.setString(2, pos);
                        preparedStatement.setString(3, lemma);
                        preparedStatement.setString(4, corpusName);
                        preparedStatement.setString(5, refCorpusName);
                        preparedStatement.setDouble(6, keyness);
                        preparedStatement.execute();
                    }
                    System.out.println("插入数据库完成");
                }
            }
        }
    }
    
    /**
     * 用LogLikeLiHood的方法计算keyness
     * @param tf
     *            单词在观察语料库中的词频
     * @param refTf
     *            单词在参考语料库中的词频
     * @param numCorpus
     *            观察语料库中不同的单词数
     * @param numRefCorpus
     *            参考语料库中不同的单词数
     * @return
     */
    private static double getLogLikeliHoodValue(int tf, int refTf, int numCorpus, int numRefCorpus) {
        double a = tf, b = refTf, c = numCorpus, d = numRefCorpus;

        if (tf == 0) {
            a = refTf;
        }
        if (refTf == 0) {
            b = refTf;
        }

        double E1, E2;
        double eCommon = (a + b) / (c + d);
        E1 = c * eCommon;
        E2 = d * eCommon;
        return 2.0 * (a * Math.log(a / E1) + b * Math.log(b / E2));
    }
    
    /**
     * 用Chi Squared的方法计算keyness
     * @param tf
     *            单词在观察语料库中的词频
     * @param refTf
     *            单词在参考语料库中的词频
     * @param numCorpus
     *            观察语料库中不同的单词数
     * @param numRefCorpus
     *            参考语料库中不同的单词数
     * @return
     */
    private static double getChiSquaredValue(int tf, int refTf, int numCorpus, int numRefCorpus) {
        double a = tf, c = numCorpus, d = numRefCorpus;
        double b = refTf;

        if (tf == 0) {
            a = MIN_VALUE;
        }
        if (refTf == 0) {
            b = MIN_VALUE;
        }

        return ((c + d) * (a * (d - b) - b * (c - a)) * (a * (d - b) - b * (c - a))) / ((a + b) * (c + d - a - b) * c * d);
    }

} 

