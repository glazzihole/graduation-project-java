package com.hugailei.graduation.corpus.util;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
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

    private static Properties props;
    private static StanfordCoreNLP  pipeline;
    static {
        props = new Properties();
        // 总共可以有tokenize, ssplit, pos, lemma, parse, ner, dcoref七中属性
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
        pipeline = new StanfordCoreNLP( props );
    }

    /**
     * 句法标注
     *
     * @param text
     * @return
     */
    public static List<CoreMap> parse( String text ) {
        List<CoreMap> result = new ArrayList<>();
        if( !StringUtils.isEmpty( text.trim() ) ) {
            Annotation document = new Annotation( text );
            pipeline.annotate( document );
            result = document.get(CoreAnnotations.SentencesAnnotation.class);
        }
        return result;
    }

    /**
     * 获取句法依存关系
     *
     * @param text
     * @return
     */
    public static List<SemanticGraphEdge> getDependency(String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<SemanticGraphEdge> dependencyResult = new ArrayList<>();
        for (CoreMap sentence : sentences) {
            //句法依存分析
            SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
            //提取依存关系
            dependencyResult.addAll(dependency.edgeListSorted());
        }

        return dependencyResult;
    }

    public static void main(String[] args) {
        String text = "she was hit by a car.";
        List<CoreMap> result = StanfordParserUtil.parse(text);
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

        List<SemanticGraphEdge> semanticGraphEdgeList = getDependency(text);
        for (SemanticGraphEdge edge : semanticGraphEdgeList) {
            System.out.println(edge.toString() + "  " + edge.getGovernor() + "  " + edge.getGovernor().index());
        }

    }

}
