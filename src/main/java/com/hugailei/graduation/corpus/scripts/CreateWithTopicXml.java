package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.dto.TopicDto;
import com.hugailei.graduation.corpus.util.FileUtil;
import com.hugailei.graduation.corpus.util.TopicClassifyUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2019/1/1
 * <p>
 * description: 对语料标注主题，生成xml文件
 * </p>
 **/
public class CreateWithTopicXml {
    private static final String NON_TOPIC_XML_PATH = "E:\\毕业论文相关\\chinadaily-xml\\";

    private static final String TEXT_PATH = "E:\\毕业论文相关\\chinadaily-text";

    private static final String WITH_TOPIC_XML_PATH = "E:\\毕业论文相关\\chinadaily-with-topic-xml\\";

    public static void main(String[] args) throws Exception{
        List<File> fileList = new ArrayList<>();
        FileUtil.getFilesUnderPath(TEXT_PATH, fileList);
        for (File file : fileList) {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder text = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line).append(" ");
            }
            List<TopicDto> topicDtoList = TopicClassifyUtil.getTopicInfoList(text.toString());
            int topicNum = topicDtoList.get(0).getTopicNum();
            System.out.println(file.getName() + "   " + topicNum);
            FileReader fileReader2 = new FileReader(new File(NON_TOPIC_XML_PATH + file.getName().replace("txt", "xml")));
            BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
            FileWriter fileWriter = new FileWriter(new File(WITH_TOPIC_XML_PATH + file.getName().replace("txt", "xml")));
            while ((line = bufferedReader2.readLine()) != null) {
                // 处理bnc的xml文件
//                String reg = "(<s n=\"[0-9_]+\")>";
//                line = line.replaceAll(reg, "$1 topic=\"" + topicNum + "\">");

                // 处理利用脚本生成的xml文件
                if (line.contains("<s n=\"")) {
                    String reg = "(<s n=\"[0-9_\\-]+\")>";
                    line = line.replaceAll(reg, "$1 topic=\"" + topicNum + "\">");
                }
                fileWriter.write(line + "\r\n");
                fileWriter.flush();
            }
        }
        System.out.println("处理完成");
    }
}
