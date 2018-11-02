package com.hugailei.graduation.corpus.util;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
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
     *
     * @param text
     * @return
     */
    public static List<CoreMap> parse( String text ) {
        List<CoreMap> result = new ArrayList<>();
        if( !StringUtils.isEmpty( text.trim() ) ) {
            Properties props = new Properties();

            //tokenize, ssplit, pos, lemma, ner, parse, dcoref
            props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
            StanfordCoreNLP  pipeline = new StanfordCoreNLP( props );
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

    /**
     * 语法树分析
     *
     * @param tree
     * @return
     */
    public static void treeAnalysis(Tree tree) {
        if (tree.isLeaf()) {
            System.out.println("leaf:" + tree.toString());
        }
        else {
            for (Tree t : tree.children()) {
                if (t.isLeaf()) {
                    System.out.println(t.toString());
                } else {
                    System.out.println("label: " + t.label());
                    if (t.label().toString().equals("S")) {
                        System.out.println(t.toString());
                    }
                    treeAnalysis(t);
                }
            }
        }
    }

    public static void main(String[] args) {
        String text = "it is where i lived three years ago.";
        List<CoreMap> result = parse(text);
        StringBuilder stringBuilder = new StringBuilder();
        // 下面的sentences 中包含了所有分析结果，遍历即可获知结果。
        for(CoreMap sentence : result) {
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            treeAnalysis(tree);
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
