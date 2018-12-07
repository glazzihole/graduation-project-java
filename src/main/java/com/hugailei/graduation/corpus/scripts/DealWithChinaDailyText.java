package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/6
 * <p>
 * description: 分离处理chinadaily语料
 * </p>
 **/
public class DealWithChinaDailyText {

    private static final String INPUT_FILE_PATH = "E:\\corpus\\ChinaDaily";
    private static final String OUTPUT_FILE_PATH = "C:\\Users\\GAILEI\\Desktop\\毕业论文相关\\chinadaily-text\\";

    public static void main(String[] args) throws Exception{
        List<File> fileList = new ArrayList<>();
        FileUtil.getFilesUnderPath(INPUT_FILE_PATH, fileList);
        int textNum = 1;
        for (File file : fileList) {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // 不以数字开头的为文章内容
                if (!line.matches("([0-9]{1,})(_).*")) {
                    System.out.println(line);
                    File textFile = new File(OUTPUT_FILE_PATH + textNum + ".txt");
                    FileWriter fileWriter = new FileWriter(textFile);
                    fileWriter.write(line);
                    fileWriter.flush();
                    fileWriter.close();
                    textNum ++;
                }
            }
        }
        System.out.println("处理完成");
    }
}
