package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HU Gailei
 * @date 2018/9/9
 * <p>
 * description:XML文件解析，获取文章标题和类型
 * </p>
 **/
@Slf4j
public class XmlDocParser {
    private static final String TITLE_FILE_PATH = "E:\\毕业论文相关\\bnc-title\\";
    private static final String TYPE_FILE_PATH = "E:\\毕业论文相关\\bnc-type\\";
    private static final String KEYWORDS_FILE_PATH = "E:\\毕业论文相关\\bnc-keyword\\";

    /**
     * 解析XML文件（主要是BNC语料文件）
     *
     * @param inputXml  待解析的xml文件
     */
    public static void parseXml(File inputXml){
        SAXReader saxReader = new SAXReader();
        try {
            //SAX生成和解析XML文档
            Document document = saxReader.read(inputXml);
            //获得根节点
            Element rootElement = document.getRootElement();
            Element teiHeader = rootElement.element("teiHeader");
            //获取文章的标题
            String title;
            try {
                title = teiHeader.element("fileDesc").element("sourceDesc").element("bibl").element("title").getText();
            } catch (NullPointerException e){
                title = teiHeader.element("fileDesc").element("titleStmt").element("title").getText();
            }

            //获取文章关键字
            List<Element> keywordsTermList = teiHeader.element("profileDesc").element("textClass").element("keywords").elements("term");
            List<String> keywordsList = keywordsTermList.stream()
                    .map(element -> element.getText().equals(" (none) ") ? "" : element.getText())
                    .collect(Collectors.toList());

            //获取文章类型和内容
            Element text = rootElement.element("wtext") != null ? rootElement.element("wtext") : rootElement.element("stext");
            String type = text.attributeValue("type");

            String fileName = inputXml.getName().replace("xml","txt");
            saveTitle(title, fileName);
            saveKeywords(keywordsList, fileName);
            saveType(type, fileName);

            System.out.println("开始解析：" + inputXml.getName());
        } catch (Exception e) {
            System.out.println("文件解析出错: " + e.getStackTrace());
        } finally {
            inputXml = null;
            saxReader = null;
        }
    }

    public static void saveTitle(String title, String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(TITLE_FILE_PATH + fileName);
        fileWriter.write(title.replace("[","").replace("]",""));
        fileWriter.flush();
        fileWriter.close();
    }

    public static void saveKeywords(List<String> keywordsList, String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(KEYWORDS_FILE_PATH + fileName);
        StringBuilder content = new StringBuilder();
        for (String keyword : keywordsList) {
            content.append(keyword.trim()).append("\r\n");
        }
        fileWriter.write(content.toString());
        fileWriter.flush();
        fileWriter.close();
    }

    public static void saveType(String type, String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(TYPE_FILE_PATH + fileName);
        fileWriter.write(type);
        fileWriter.flush();
        fileWriter.close();
    }

    public static void main(String[] args) {
        String documentPath = "E:\\corpus\\一些语料\\BNC\\2554\\download\\Texts";
        List<File> allFile = new ArrayList<>();
        List<File> filePathList = FileUtil.getFilesUnderPath(documentPath, allFile);
        for(File file : filePathList){
            parseXml(file);
        }
    }
}
