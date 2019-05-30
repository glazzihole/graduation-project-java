package com.hugailei.graduation.corpus.scripts;

import com.bfsuolframework.core.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/20
 * <p>
 * description: 处理搜狗新闻语料，主题分类用
 * </p>
 **/
public class DealWithSogouNews {
    private static final String FILE_PATH = "E:\\毕业论文相关\\fasttext-train\\sogounews\\news_tensite.txt";
    private static final String GOOGLE_TRANSLATE_URL = "http://localhost:8080/support/youdao-dict";
    private static final String TRAINNING_FILE_PATH = "E:\\毕业论文相关\\fasttext-train\\temp1.txt";

    public static void main(String[] args) throws Exception {
//        label();
        classify();
    }

    /**
     * 把语料按照指定格式进行标注
     */
    private static void label() throws Exception {
        System.out.println("标注开始");
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        File file = new File(FILE_PATH);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "GBK");
        BufferedReader bufferedWriter = new BufferedReader(isr);

//        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TRAINNING_FILE_PATH, true)));
        FileWriter writer = new FileWriter(new File(TRAINNING_FILE_PATH));
        String line = "", url = "", content = "";
        while ((line = bufferedWriter.readLine()) != null) {
            try {
                if (line.startsWith("http")) {
                    url = line;
                }
                if (line.startsWith("__label__")) {
                    String temp = line.replaceAll("__label__", "");
                    if ((url.contains("finance") || url.contains("entert_")) && !StringUtils.isBlank(temp.trim())) {
                        content = line.replaceAll("__label__", "").replaceAll(" +", "").replaceAll("　", "");
                        int retryTime = 1;
                        boolean failed = true;
                        URIBuilder uri = new URIBuilder(GOOGLE_TRANSLATE_URL + "?from=zh&to=en&text=" + content);
                        HttpPost httpPost = new HttpPost(uri.toString());
                        CloseableHttpResponse response = null;
                        while (failed && retryTime <= 3) {
                            try {
                                response = httpClient.execute(httpPost);
                                failed = false;
                            } catch (IOException e) {
                                long waitTime = 10000 * retryTime;
                                System.out.println("网络超时，等待" + waitTime / 1000 + "秒后重试第" + retryTime + "次:" + uri.toString());
                                Thread.sleep(waitTime);
                                retryTime++;
                                if (retryTime > 3) {
                                    throw e;
                                }
                            }
                        }

                        String result = EntityUtils.toString(response.getEntity());
                        System.out.println("entity: " + result);
                        if (!StringUtils.isBlank(result)) {
                            ResponseVO responseVO = new ObjectMapper().readValue(result.getBytes(), ResponseVO.class);
                            HashMap<String, Object> youdaoOpenApiResponse = (HashMap<String, Object>) responseVO.getData();
                            List<String> translationList = (List<String>) youdaoOpenApiResponse.get("translation");
                            String sentence = "__label__5 " + translationList.get(0);
                            if (url.contains("entert_")) {
                                sentence = "__label__6 " + translationList.get(0);
                            }
                            System.out.println(sentence);
                            if (responseVO.getData() != null) {
                                writer.write(sentence.replaceAll("【", "") + "\r\n");
                                writer.flush();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("出错了，不过我还能挺住");
                continue;
            }
        }
        System.out.println("标注完成");
    }

    /**
     * 抽取出指定金融和娱乐主题，并且分类存储
     *
     * @throws Exception
     */
    private static void classify() throws Exception {
        System.out.println("分类开始");
        File file = new File(FILE_PATH);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "GBK");
        BufferedReader bufferedWriter = new BufferedReader(isr);

        String financeFilePath = "E:\\毕业论文相关\\fasttext-train\\sogounews\\finance";
        String entertFilePath = "E:\\毕业论文相关\\fasttext-train\\sogounews\\entert";
        FileWriter financeFileWriter = new FileWriter(new File(financeFilePath + "1.txt"));
        FileWriter entertFileWriter = new FileWriter(new File(entertFilePath + "1.txt"));
        String line = "", url = "", content = "";
        int financeFileLineNum = 1, entertFileLineNum = 1;
        int financeLineCount = 1, entertLineCount = 1;
        while ((line = bufferedWriter.readLine()) != null) {
            try {
                if (line.startsWith("http")) {
                    url = line;
                }
                if (line.startsWith("__label__")) {
                    String temp = line.replaceAll("__label__", "");
                    if ((url.contains("finance") || url.contains("entert_")) && !StringUtils.isBlank(temp.trim())) {
                        content = line.replaceAll("__label__", "").replaceAll(" +", "").replaceAll("　", "");
                        System.out.println(content);
                        if (url.contains("finance")) {
                            financeLineCount ++;
                            if (financeFileLineNum == 400) {
                                financeFileLineNum = 1;
                                int tempNum = financeLineCount / 400;
                                financeFileWriter = new FileWriter(new File(financeFilePath + tempNum + ".txt"));
                            } else {
                                financeFileLineNum ++;
                            }
                            financeFileWriter.write(content + "\r\n");
                            financeFileWriter.flush();
                        } else {
                            entertLineCount ++;
                            if (entertFileLineNum == 400) {
                                entertFileLineNum = 1;
                                int tempNum = entertLineCount / 400;
                                entertFileWriter = new FileWriter(new File(entertFilePath + tempNum + ".txt"));
                            } else {
                                entertFileLineNum ++;
                            }
                            entertFileWriter.write(content + "\r\n");
                            entertFileWriter.flush();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("出错了，不过我还能挺住");
                continue;
            }
        }
        System.out.println("分类完成");
    }
}
