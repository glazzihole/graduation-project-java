package com.hugailei.graduation.corpus.util;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
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
import java.util.Map;
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
    private static StanfordCoreNLP relationPipeline;
    static {
        props = new Properties();
        // 总共可以有tokenize, ssplit, pos, lemma, parse, ner, dcoref七中属性
        props.put("annotators", "tokenize, ssplit, parse, pos, lemma");
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

    /**
     * 获取基本词性，如VBZ还原为VB
     * @param pos
     * @return
     */
    public static String getBasePos(String pos) {
        for (Map.Entry entry : CorpusConstant.POS_REGEX_TO_LEMMA_POS.entrySet()) {
            String posRegex = (String) entry.getKey();
            String basePos = (String) entry.getValue();
            if (pos !=null && pos.matches(posRegex)) {
                return basePos;
            }
        }
        return pos;
    }

    /**
     * 包含关系抽取的句法分析
     *
     * @param text
     * @return
     */
    public static List<CoreMap> relationParse(String text) {
        if (relationPipeline == null) {
            props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse, natlog, openie, dcoref, relation");
            relationPipeline = new StanfordCoreNLP( props );
        }
        List<CoreMap> result = new ArrayList<>();
        if( !StringUtils.isEmpty( text.trim() ) ) {
            Annotation document = new Annotation( text );
            relationPipeline.annotate( document );
            result = document.get(CoreAnnotations.SentencesAnnotation.class);
        }
        return result;
    }

    public static void main(String[] args) {
        String text = "I do remember the day I met you.";
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

//        result = relationParse(text);
//        for (CoreMap sentence : result) {
//            Collection<RelationTriple> realtions = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
////            List<RelationMention> realtions = sentence.get(MachineReadingAnnotations.RelationMentionsAnnotation.class);
//            for (RelationTriple relation : realtions) {
//                System.out.println(relation.subjectGloss() + " " + relation.relationGloss() + " " + relation.objectGloss());
//            }
//        }

    }

}
