package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2019/3/6
 * <p>
 * description: 对文件中的句子进行筛选，只选择符合条件的句子作为当前等级的例句
 * </p>
 **/
public class FilterSentence {
    private static String INPUT_FILE_PATH = "";
    private static String OUTPUT_FILE_PATH1 = "";
    private static String OUTPUT_FILE_PATH2 = "";

    public static void main(String[] args) throws Exception{
        FileReader fileReader = new FileReader(new File(INPUT_FILE_PATH));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            List<CoreMap> result = StanfordParserUtil.parse(line);
            for (CoreMap sentence : result) {
                int sentenceLength = sentence.get(CoreAnnotations.TokensAnnotation.class).size();
                if (sentenceLength > 15) {

                }
            }
        }
    }
}
