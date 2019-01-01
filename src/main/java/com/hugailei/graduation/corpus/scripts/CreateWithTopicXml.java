package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.dto.TopicDto;
import com.hugailei.graduation.corpus.util.FileUtil;
import com.hugailei.graduation.corpus.util.TopicClassifyUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    private static final String NON_TOPIC_XML_PATH = "";

    private static final String WITH_TOPIC_XML_PATH = "";

    public static void main(String[] args) throws Exception{
        List<File> fileList = new ArrayList<>();
        FileUtil.getFilesUnderPath(NON_TOPIC_XML_PATH, fileList);
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

            bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("<s n=\"")) {
                    line.replace("(<s n=\"[0-9]{1,}\">)", "$1<topic = \"" + topicNum + "\">");
                    line.replace("</s>", "</topic></s>");
                }
            }
        }
    }
}
