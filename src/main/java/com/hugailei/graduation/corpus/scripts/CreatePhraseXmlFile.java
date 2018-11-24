package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.FileUtil;
import com.hugailei.graduation.corpus.util.SentencePatternUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
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
 * @date 2018/11/24
 * <p>
 * description: 抽取短语，并生成短语的xml格式文件
 * </p>
 **/
public class CreatePhraseXmlFile {
    private static final String INPUT_FILE_PATH = "E:\\毕业论文相关\\bnc-sample-text";
    private static final String OUTPUT_FILE_PATH = "E:\\毕业论文相关\\bnc-phrase-xml";

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

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("line: " + line);
                List<CoreMap> sentenceList = StanfordParserUtil.parse(line);

                for (CoreMap sentence : sentenceList) {
                    List<List<SentencePatternUtil.Edge>> edgeListList = SentencePatternUtil.matchPhrase(sentence);

                    // 将短语及标注信息写入XML文件
                    int phraseId = 0;
                    for (List<SentencePatternUtil.Edge> edgeList : edgeListList) {

                        // XML处理相关
                        Element tei = new Element("TEI");
                        tei.setAttribute("id", textId + "-" + sentenceId + "-" + phraseId);

                        Element text = new Element("text");
                        Element body = new Element("body");
                        Element p = new Element("p");
                        text.setAttribute("id", textId + "-" + sentenceId + "-" + phraseId);
                        Element s = new Element("s");
                        s.setAttribute("n", textId + "-" + sentenceId + "-" + phraseId );

                        int wordNum = 0;
                        for (SentencePatternUtil.Edge edge : edgeList) {
                            Element w = new Element("w");
                            w.setAttribute("lemma", edge.getLemma());
                            w.setAttribute("type", edge.getPos());
                            w.setAttribute( "id", (wordNum++) + "" );
                            w.addContent( edge.getWord() );
                            s.addContent( w );
                        }
                        p.addContent( s );
                        body.addContent( p );
                        text.addContent( body );
                        tei.addContent(text);
                        teiCorpus.addContent( tei );

                        phraseId ++;
                    }
                    sentenceId ++;
                } // for (CoreMap sentence : sentenceList)

            } // while

            //输出标注好的语料
            Format format = Format.getPrettyFormat();
            XMLOutputter XMLOut = new XMLOutputter(format);
            OutputStream out = new FileOutputStream( OUTPUT_FILE_PATH + "\\" + file.getName().replace("txt", "xml"));
            XMLOut.output(xmlDoc, out);

            textId ++;
        } // for (File file : fileList)
    }
}
