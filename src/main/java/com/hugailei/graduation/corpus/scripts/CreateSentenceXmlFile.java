package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.FileUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/28
 * <p>
 * description: 对句子进行标注，并生成xml格式文件
 * </p>
 **/
public class CreateSentenceXmlFile {
    private static final String INPUT_FILE_PATH = "C:\\Users\\GAILEI\\Desktop\\毕业论文相关\\chinadaily-text";
    private static final String OUTPUT_FILE_PATH = "C:\\Users\\GAILEI\\Desktop\\毕业论文相关\\chinadaily-xml";

    public static void main(String[] args) throws Exception{
        //读取语料
        List<File> fileList = new ArrayList<>();
        fileList = FileUtil.getFilesUnderPath(INPUT_FILE_PATH, fileList);
        int textId = 0;
        for (File file : fileList) {
            int sentenceId = 0;
            System.out.println("开始分析" + file.getCanonicalPath());
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";

            // XML相关
            Element teiCorpus = new Element("teiCorpus");
            Document xmlDoc = new Document(teiCorpus);
            Element tei = new Element("TEI");
            tei.setAttribute("id", textId + "");

            Element text = new Element("text");
            text.setAttribute("id", textId + "");
            Element body = new Element("body");
            body.setAttribute("id", textId + "");
            Element p = new Element("p");
            p.setAttribute("id", textId + "");

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("line: " + line);
                List<CoreMap> sentenceList = StanfordParserUtil.parse(line);

                for (CoreMap sentence : sentenceList) {
                    Element s = new Element("s");
                    s.setAttribute("n", textId + "-" + sentenceId);
                    int wordNum = 0;
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        Element w = new Element("w");
                        w.setAttribute("lemma", token.lemma());
                        w.setAttribute("type", token.tag());
                        w.setAttribute("id", (wordNum++) + "");
                        w.addContent(token.word());
                        s.addContent(w);
                    }
                    p.addContent(s);
                    sentenceId ++;
                } // for (CoreMap sentence : sentenceList)

            } // while

            //输出标注好的语料
            body.addContent(p);
            text.addContent(body);
            tei.addContent(text);
            teiCorpus.addContent(tei);
            Format format = Format.getPrettyFormat();
            XMLOutputter XMLOut = new XMLOutputter(format);
            OutputStream out = new FileOutputStream( OUTPUT_FILE_PATH + "\\" + file.getName().replace("txt", "xml"));
            XMLOut.output(xmlDoc, out);

            textId ++;
        } // for (File file : fileList)
    }
}
