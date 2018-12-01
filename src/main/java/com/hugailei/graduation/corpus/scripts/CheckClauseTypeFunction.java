package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.domain.SentencePattern;
import com.hugailei.graduation.corpus.util.SentencePatternUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/26
 * <p>
 * description: 句型匹配准确率校验
 * </p>
 **/
public class CheckClauseTypeFunction {
    private static final String SENTENCE_FILE_PATH = "C:\\Users\\GAILEI\\Desktop\\毕业论文相关\\句型判断\\同位语从句.txt";
    private static final String RESULT_FILE_PATH = "C:\\Users\\GAILEI\\Desktop\\毕业论文相关\\句型判断\\同位语从句-结果.txt";

    public static void main(String[] args) throws Exception{
        FileReader fileReader = new FileReader(new File(SENTENCE_FILE_PATH));
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        FileWriter fileWriter = new FileWriter(new File(RESULT_FILE_PATH));
        String sentence;
        while((sentence = bufferedReader.readLine()) != null) {
            List<CoreMap> coreMapList = StanfordParserUtil.parse(sentence);
            List<SentencePattern> sentencePatternList = SentencePatternUtil.matchAppositiveClauseOrAttributiveClause(coreMapList.get(0));
            if (sentencePatternList != null){
                SentencePattern sentencePattern = sentencePatternList.get(0);
                int type = sentencePattern.getType();
                switch (type) {
                    case 1:
                        fileWriter.write("主语从句\r\n");
                        break;

                    case 2:
                        fileWriter.write("宾语从句\r\n");
                        break;

                    case 3:
                        fileWriter.write("定语从句或同位语从句\r\n");
                        break;

                    case 4:
                        fileWriter.write("表语从句\r\n");
                        break;

                    case 5:
                        fileWriter.write("状语从句\r\n");
                        break;

                    case 6:
                        fileWriter.write("被动句\r\n");
                        break;

                    case 7:
                        fileWriter.write("双宾语句\r\n");
                        break;

                    default:
                        fileWriter.write("其他\r\n");
                        break;
                }
            } else {
                fileWriter.write("其他\r\n");
            }
            fileWriter.flush();
        }

        fileWriter.close();
    }
}
