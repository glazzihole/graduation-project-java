package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/11
 * <p>
 * description: 各类句型匹配
 * </p>
 **/
public class SentencePatternUtil {

    /**
     * 匹配句法树中的主语从句
     *
     * @param sentence
     */
    public static void matchSubjectClause(CoreMap sentence) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        // 匹配规则
        TregexPattern pattern = TregexPattern.compile("SBAR $++ VP");
        // 匹配查找
        TregexMatcher matcher = pattern.matcher(tree);
        // 匹配输出
        while (matcher.findNextMatchingNode()) {
            Tree match = matcher.getMatch();
            //match.pennPrint();
            // TODO
            System.out.println("该句为主语从句");
        }
    }

    /**
     * 匹配句法树中的宾语从句或者表语从句
     *
     * @param sentence
     */
    public static void matchObjectClauseOrPredicativeClause(CoreMap sentence) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        // 匹配规则
        TregexPattern pattern = TregexPattern.compile("VP << /^VB.*$/ & << SBAR");
        // 匹配查找
        TregexMatcher matcher = pattern.matcher(tree);
        // 匹配输出
        while (matcher.findNextMatchingNode()) {
            //是否为表语从句的标识
            boolean isPredicativeClause = false;
            Tree match = matcher.getMatch();
            // 获取匹配到的树结构的子节点
            Tree[] childrens = match.children();
            // 获取动词
            String verbReg = "VB.*";
            for (Tree children : childrens) {
                if (children.label().toString().matches(verbReg)) {
                    // 获取当前单词在句子中是第几个单词（从0开始）
                    int index = children.getLeaves().get(0).indexLeaves(1, false) - 2;
                    // 根据index获取动词的原型
                    CoreLabel coreLabel = sentence.get(CoreAnnotations.TokensAnnotation.class).get(index);
                    String lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);
                    // 判断该动词是否为系统词
                    if (CorpusConstant.COPULA_LEMMA_SET.contains(lemma)) {
                        isPredicativeClause = true;
                        break;
                    }
                }
            }
            if (isPredicativeClause) {
                System.out.println("该句为表语从句");
            } else {
                System.out.println("该句为宾语从句");
            }
        }
    }

    /**
     * 匹配句法树中的同位语从句
     *
     * @param sentence
     */
    public static void matchAppositiveClauseOrAttributiveClause(CoreMap sentence) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        // 匹配规则
        TregexPattern pattern = TregexPattern.compile("NP $.. SBAR");
        // 匹配查找
        TregexMatcher matcher = pattern.matcher(tree);
        // 匹配输出
        while (matcher.findNextMatchingNode()) {
            boolean isAppositiveClause = false;
            Tree match = matcher.getMatch();
            Tree[] childrens = match.children();
            // 获取从句所修饰的名词
            String nounReg = "NN.*";
            for (Tree children : childrens) {
                if (children.label().toString().matches(nounReg)) {
                    // 获取该名词的index
                    int index = children.getLeaves().get(0).indexLeaves(1, false) - 2;
                    // 通过index获取名词的原型
                    CoreLabel coreLabel = sentence.get(CoreAnnotations.TokensAnnotation.class).get(index);
                    String lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);
                    // 判断该名词是否为同位语从句的先行词
                    if (CorpusConstant.APPOSITIVE_ANTECEDENT_SET.contains(lemma)) {
                        isAppositiveClause = true;
                        break;
                    }
                }
            }
            if (isAppositiveClause) {
                System.out.println("该句为同位语从句");
            } else {
                System.out.println("该句为定语从句");
            }
        }
    }

    /**
     * 匹配状语从句
     *
     * @param sentence
     */
    public static void matchAdverbialClause(CoreMap sentence) {
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        // 判断依存关系中是否包含advcl:XXX
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            String relation = edge.getRelation().toString();
            if (relation.startsWith("advcl")) {
                System.out.println("该句为状语从句");
            }
        }
    }



    /**
     * 匹配被动语态
     *
     * @param sentence
     */
    public static void matchPassiveVoice(CoreMap sentence) {
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        // 判断依存关系中是否包含nsubjpass
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            String relation = edge.getRelation().toString();
            if (relation.startsWith("nsubjpass")) {
                System.out.println("该句为被动语态");
            }
        }
    }

    /**
     * 匹配双宾语句
     *
     * @param sentence
     */
    public static void matchDoubleObjects(CoreMap sentence) {
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        // 判断依存关系中是否包含iobj(间接宾语)和dobj(直接宾语)
        boolean hasIobj = false;
        boolean hasDobj = false;
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            String relation = edge.getRelation().toString();
            if (relation.startsWith("iobj")) {
                hasIobj = true;
            } else if (relation.startsWith("dobj")) {
                hasDobj = true;
            }

            if (hasIobj && hasDobj) {
                break;
            }
        }

        if (hasIobj && hasDobj) {
            System.out.println("该句为双宾语句");
        }
    }

    /**
     * 匹配各类短语
     *
     * @param sentence
     */
    public static void matchPhrase(CoreMap sentence) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        for (String label : CorpusConstant.PHRASE_LABEL_SET) {
            // 匹配规则
            TregexPattern pattern = TregexPattern.compile(label);
            // 匹配查找
            TregexMatcher matcher = pattern.matcher(tree);
            // 匹配输出
            while (matcher.findNextMatchingNode()) {
                Tree match = matcher.getMatch();
                StringBuilder phrase = new StringBuilder();
                // 排除掉单个单词
                if (match.getLeaves().size() > 1) {
                    for (Tree leaf : match.getLeaves()) {
                        phrase.append(leaf.label().value()).append(" ");
                    }
                    System.out.println(phrase.toString());
                }
            }
        }
    }

    public static void main(String[] args) {
        String text = "there lies a book on the desk.";
        List<CoreMap> result = StanfordParserUtil.parse(text);

        for(CoreMap sentence : result) {
            System.out.println(sentence.get(TreeCoreAnnotations.TreeAnnotation.class));
            matchPhrase(sentence);
        }
    }
}
