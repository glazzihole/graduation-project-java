package com.hugailei.graduation.corpus.scripts;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hugailei.graduation.corpus.util.FileUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * @author HU Gailei
 * @date 2018/11/28
 * <p>
 * description: 抽取有道单词双语例句中的例句，存储到文件中
 * </p>
 **/
public class ExtractSentenceFromYouDao {
    private static final String YOUDAO_JSON_FILE_PATH = "E:\\资源\\有道单词例句";
    private static final String OUT_PUT_FILE_PATH = "C:\\Users\\GAILEI\\Desktop\\毕业论文相关\\youdao-text\\";

    private static final String LANGUAGE = "english";

    public static void main(String[] args) throws Exception{
        List<File> fileList = new ArrayList<>();
        fileList = FileUtil.getFilesUnderPath(YOUDAO_JSON_FILE_PATH, fileList);
        Set<String> sentenceSet = new HashSet<>();
        for (File file : fileList) {
            FileUtils.readFileToString(file,"UTF-8");
            System.out.println("正在读取：" + file.getAbsolutePath());
            String jsonString = FileUtils.readFileToString(file,"UTF-8");
            System.out.println("读取完毕，开始解析");

            JSONObject jsonObject = (JSONObject) JSONObject.parse(jsonString);
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                JSONArray jsonArray= (JSONArray) entry.getValue();
                for (int i=0; i < jsonArray.size(); i++) {
                    JSONObject sentenceObject = jsonArray.getJSONObject(i);
                    String sentence = sentenceObject.getString(LANGUAGE);
                    sentenceSet.add(sentence);
                }
            }
        }

        int textId = 1;
        int sentenceId = 1;
        FileWriter fileWriter = new FileWriter(new File(OUT_PUT_FILE_PATH + textId + ".txt"));
        for (String line : sentenceSet) {
            if (sentenceId <= 2000) {
                System.out.println(line);
                fileWriter.write(line + "\r\n");
                fileWriter.flush();
            }
            else {
                textId ++;
                sentenceId = 1;

                fileWriter = new FileWriter(new File(OUT_PUT_FILE_PATH + textId + ".txt"));
                fileWriter.write(line + "\r\n");
                fileWriter.flush();
            }
            sentenceId ++;
        }
        System.out.println("处理完成");
    }
}
