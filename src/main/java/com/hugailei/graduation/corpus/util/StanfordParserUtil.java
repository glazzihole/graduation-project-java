package com.hugailei.graduation.corpus.util;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author HU Gailei
 * @date 2018/9/8
 * <p>
 *     description: 句法分析工具
 * </p>
 **/
public class StanfordParserUtil {

    /**
     * 句法标注，标注单词词性，单词原型
     * @param text
     * @return
     */
    public static List<CoreMap> parse( String text ) {
        List<CoreMap> result = new ArrayList<>();
        if( !StringUtils.isEmpty( text.trim() ) ) {
            Properties props = new Properties();

            //tokenize, ssplit, pos, lemma, ner, parse, dcoref
            props.put("annotators", "tokenize, ssplit, pos, lemma");
            StanfordCoreNLP  pipeline = new StanfordCoreNLP( props );
            Annotation document = new Annotation( text );
            pipeline.annotate( document );

            result = document.get(CoreAnnotations.SentencesAnnotation.class);
        }
        return result;
    }

    public static void main(String[] args) {
        String text = "she was a beautiful girl.";
        List<CoreMap> result = parse(text);
        StringBuilder stringBuilder = new StringBuilder();
        // 下面的sentences 中包含了所有分析结果，遍历即可获知结果。
        for(CoreMap sentence : result) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // 获取单词
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                stringBuilder.append("word = " + word + " | ");
                // 获取词性标注
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                stringBuilder.append("pos = " + pos + " | ");
                //获取原型标注结果
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                stringBuilder.append("lemma = " + lemma + " " + "\r\n");
            }
        }
        System.out.println(stringBuilder.toString());
    }

}
