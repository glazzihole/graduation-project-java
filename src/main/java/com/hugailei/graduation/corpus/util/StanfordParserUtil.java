package com.hugailei.graduation.corpus.util;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.util.StringUtils;

import java.util.*;

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
        props.put("annotators", "tokenize, ssplit, parse, pos, lemma, ner");
        pipeline = new StanfordCoreNLP(props);
    }

    /**
     * 句法标注
     *
     * @param text
     * @return
     */
    public static List<CoreMap> parse(String text) {
        List<CoreMap> result = new ArrayList<>();
        if( !StringUtils.isEmpty(text.trim())) {
            Annotation document = new Annotation(text);
            pipeline.annotate(document);
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
        if( !StringUtils.isEmpty(text.trim())) {
            Annotation document = new Annotation(text);
            relationPipeline.annotate(document);
            result = document.get(CoreAnnotations.SentencesAnnotation.class);
        }
        return result;
    }


    private static void sortRelationTripleList(List<RelationTriple> relationTripleList) {
        Collections.sort(relationTripleList, new Comparator<RelationTriple>() {
            @Override
            public int compare(RelationTriple c1, RelationTriple c2) {
                if (c2.confidence > c1.confidence) {
                    return 1;
                } else if (c2.confidence == c1.confidence) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
    }


    public static void main(String[] args) {
        String[] textArray = {"David said that he will arrive United States on Sunday."};
        int i = 0;
        for (String text : textArray) {
            List<CoreMap> result = StanfordParserUtil.parse(text);
            StringBuilder stringBuilder = new StringBuilder();
            // 下面的sentences 中包含了所有分析结果，遍历即可获知结果。
            for (CoreMap sentence : result) {
                // 原型、词性等信息
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
                    String ne = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
                    stringBuilder.append("ne = " + ne + " " + "\r\n");
                    String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                    stringBuilder.append("ner = " + ner + " " + "\r\n");
                }
                System.out.println(stringBuilder.toString());

                // 依存关系信息
                List<SemanticGraphEdge> semanticGraphEdgeList = getDependency(sentence.toString());
                for (SemanticGraphEdge edge : semanticGraphEdgeList) {
                    System.out.println(edge.toString() + "  " + edge.getGovernor() + "  " + edge.getGovernor().index());
                }

                // 关系提取
//                result = relationParse(sentence.toString());
//                for (CoreMap s : result) {
//                    List<RelationTriple> realtions = new ArrayList<>(s.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class));
//                    sortRelationTripleList(realtions);
//                    double confidence = 0;
//                    // 最短的句子长度
//                    int shortest = s.toString().length();
//                    // 最长的句子长度
//                    int longest = 0;
//                    for (RelationTriple relation : realtions) {
//                        String temp = relation.subjectGloss() + " " + relation.relationGloss() + " " + relation.objectGloss();
//                        System.out.println(temp);
//                        // 找出“可信度”最高的一批
//                        if (relation.confidence >= confidence) {
//                            confidence = relation.confidence;
//                            if (temp.split(" ").length >= longest) {
//                                longest = temp.split(" ").length;
//                            }
//                            if (temp.split(" ").length <= shortest) {
//                                shortest =temp.split(" ").length;
//                            }
//                        }
//                    }
//                    System.out.println("__________");
//                    for (RelationTriple relation : realtions) {
//                        String temp = relation.subjectGloss() + " " + relation.relationGloss() + " " + relation.objectGloss();
//                        if (relation.confidence >= confidence) {
//                            System.out.println(temp);
////                            if (temp.split(" ").length == longest) {
////                                System.out.println(temp);
////                            }
////                            if (temp.split(" ").length == shortest) {
////                                System.out.println(temp);
////                            }
//                        } else {
//                            break;
//                        }
//                    }
//                }
            }
        }

    }
}
