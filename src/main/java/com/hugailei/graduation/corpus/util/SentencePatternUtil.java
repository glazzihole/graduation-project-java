package com.hugailei.graduation.corpus.util;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
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
                    // 获取该名词的index，从0开始
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

    /**
     * 匹配被从句修饰的词
     *
     * @param sentence
     */
    public static void matchModificand(CoreMap sentence) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        // 先找出从句（SBAR）
        TregexPattern pattern = TregexPattern.compile("SBAR");
        TregexMatcher matcher = pattern.matcher(tree);

        while (matcher.findNextMatchingNode()) {
            Tree match = matcher.getMatch();
            // 获取从句起始位置，从0开始计数
            int clauseIndex = match.getLeaves().get(0).indexLeaves(1, false) - 2;
            System.out.println(clauseIndex);

            // 获取从句（字符串）
            String clause = "";
            for (Tree t: match.getLeaves()) {
                clause = clause + t.label().value() + " ";
            }
            // 获取其父节点
            Tree parent = match.parent(tree);
            String parentLabel = parent.label().value();
            TregexPattern leafPat = null;
            TregexPattern leafPat2 = null;
            switch (parentLabel){
                case "NP":
                    leafPat = TregexPattern.compile("/^NN.*$/ == /^NN.*$/");
                    break;
                case "VP":
                    leafPat = TregexPattern.compile("/^VB.*$/ == /^VB.*$/");
                    leafPat2 = TregexPattern.compile("/^RB.*$/ == /^RB.*$/");
                    break;
                case "ADJP":
                    leafPat = TregexPattern.compile("/^JJ.*$/ == /^JJ.*$/");
                    leafPat2 = TregexPattern.compile("/^RB.*$/ == /^RB.*$/");
                    break;
                case "ADVP":
                    leafPat = TregexPattern.compile("/^RB.*$/ == /^RB.*$/");
                    break;
                default:
                    break;
            }
            if (leafPat != null) {
                String modificand = null, modificand2 = null, lemma = null, lemma2 = null, pos = null, pos2 = null;
                int modificandIndex = 0, modificand2Index = 0;

                // 匹配被修饰的词和其在句中的位置
                TregexMatcher leafMatcher = leafPat.matcher(tree);
                while (leafMatcher.findNextMatchingNode()) {
                    int tempIndex = leafMatcher.getMatch().getLeaves().get(0).indexLeaves(1, false) - 2;
                    if (tempIndex >= clauseIndex) {
                        break;
                    }
                    modificand = leafMatcher.getMatch().getLeaves().get(0).label().value();
                    modificandIndex = tempIndex;

                    // 根据修饰词在句中的位置获取其词性及原型
                    CoreLabel coreLabel = sentence.get(CoreAnnotations.TokensAnnotation.class).get(modificandIndex);
                    lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);
                    pos = coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    // 取词性的基本类型

                }

                if (leafPat2 != null) {
                    TregexMatcher leafMatcher2 = leafPat2.matcher(tree);
                    while (leafMatcher2.findNextMatchingNode()) {
                        int tempIndex = leafMatcher2.getMatch().getLeaves().get(0).indexLeaves(1, false) - 2;
                        if (tempIndex >= clauseIndex) {
                            break;
                        }
                        modificand2 = leafMatcher2.getMatch().getLeaves().get(0).label().value();
                        modificand2Index = tempIndex;
                        CoreLabel coreLabel = sentence.get(CoreAnnotations.TokensAnnotation.class).get(modificand2Index);
                        pos2 = coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        // 取词性的基本类型

                        lemma2 = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);
                    }
                }

                System.out.println("从句:" + clause);
                System.out.println("被修饰词：" + modificand);
                System.out.println("原型：" + lemma);
                System.out.println("词性：" + pos);

                System.out.println("被修饰词2：" + modificand2);
                System.out.println("原型2：" + lemma2);
                System.out.println("词性2：" + pos2);

            }

        }// while - match

    }

    public static void main(String[] args) {
        String text = "The book that I bought yesterday was written by Lu Xun.";
        List<CoreMap> result = StanfordParserUtil.parse(text);

        for(CoreMap sentence : result) {
            System.out.println(sentence.get(TreeCoreAnnotations.TreeAnnotation.class));
            matchModificand(sentence);
        }
    }
}
