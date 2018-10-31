package com.hugailei.graduation.corpus.util;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author HU Gailei
 * @date 2018/10/4
 * <p>
 * description: 依存句法分析
 * </p>
 **/
public class StanfordDependencyUtil {

    public static List<SemanticGraphEdge> getDependency(String text) {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
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
        String text = "she is the girl who sings the best in our class.";
        List<SemanticGraphEdge> dependencyResult = getDependency(text);
        for (SemanticGraphEdge edge : dependencyResult) {
            //获取依存类型
            String dependencyType = edge.getRelation().toString();
            System.out.println(edge.toString());
        }//for
    }
}
