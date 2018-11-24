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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/11
 * <p>
 * description: 各类句型匹配及提取
 * </p>
 **/
public class SentencePatternUtil {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        String word;
        String lemma;
        String pos;
        int index;
    }

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
        // 判断依存关系中是否包含iobj(间接宾语)
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            String relation = edge.getRelation().toString();
            if (relation.startsWith("iobj")) {
                System.out.println("该句为双宾语句");
                break;
            }
            // 可能存在无法直接通过iobj识别的情况，可再根据动词等情况进一步判断
            else {
                String govAndDep = edge.getGovernor().tag() + "-" + edge.getDependent().tag();
                if (govAndDep.matches("(VB[A-Z]{0,1})-(NN[A-Z]{0,1})") &&
                        CorpusConstant.DOUBLE_OBJECT_VERB_SET.contains(edge.getGovernor().lemma().toLowerCase())){
                    int verbIndex = edge.getGovernor().index();
                    int directObjectIndex = edge.getDependent().index();
                    // 寻找nsubj依存关系，找出主语
                    for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                        if (semanticGraphEdge.getRelation().toString().equals("nsubj") &&
                                semanticGraphEdge.getGovernor().index() == verbIndex) {

                            // 寻找nsubj依存关系，找出间接宾语
                            for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                if (se.getRelation().toString().equals("nsubj") &&
                                        se.getGovernor().index() == directObjectIndex) {
                                    System.out.println("该句为双宾语句");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 匹配各类短语
     *
     * @param sentence
     */
    public static List<List<Edge>> matchPhrase(CoreMap sentence) {
        List<List<Edge>> resultList = new ArrayList<>();
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
                // 排除掉单个单词，过长的短语排除掉
                if (match.getLeaves().size() > 1 && match.getLeaves().size() <= 10) {
                    List<Edge> edgeList = new ArrayList<>();
                    for (Tree leaf : match.getLeaves()) {
                        int index = leaf.indexLeaves(1, false) - 2;
                        // 根据index获取单词的原型、词性
                        CoreLabel coreLabel = sentence.get(CoreAnnotations.TokensAnnotation.class).get(index);
                        String lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);
                        String pos = coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        String word = leaf.label().value();
                        edgeList.add(new Edge(word, lemma, pos, index));

                    }
                    resultList.add(edgeList);
                }
            }
        }
        return resultList;
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

    /**
     * 提取句子主干
     *
     * @param sentence
     */
    public static void matchPrincipalClause(CoreMap sentence) {
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);

        // 以下所有的查找中，各复合名词修饰还未处理 todo

        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            String govAndDep = edge.getGovernor().tag() + "-" + edge.getDependent().tag();
            switch (edge.getRelation().toString()) {
                case "nsubj":
                    // 匹配是否包含主谓宾结构
                    if (govAndDep.matches("(VB[A-Z]{0,1})-(NN[A-Z]{0,1})") ||
                            govAndDep.matches("(VB[A-Z]{0,1})-PRP")) {
                        Edge temp = getRealEdge(edge.getDependent().index(), dependency);
                        String subject = (temp == null ? edge.getDependent().word() : temp.getWord());
                        String predicate = edge.getGovernor().word();
                        String object = null;
                        int predicateIndex = edge.getGovernor().index();
                        // 是否有宾语的标识
                        boolean hasObject = false;
                        boolean isDoubleObject = false;
                        // 寻找dobj依存关系，找出动词的宾语
                        for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                            if (semanticGraphEdge.getRelation().toString().equals("dobj")) {
                                String objGovAndDep = semanticGraphEdge.getGovernor().tag() + "-" + semanticGraphEdge.getDependent().tag();
                                if (objGovAndDep.matches("(VB[A-Z]{0,1})-(NN[A-Z]{0,1})") ||
                                        objGovAndDep.matches("(VB[A-Z]{0,1})-PRP")) {
                                    // 通过单词位置判断是否为同一个谓语
                                    if (semanticGraphEdge.getGovernor().index() == predicateIndex) {
                                        temp = getRealEdge(semanticGraphEdge.getDependent().index(), dependency);
                                        object = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());
                                        hasObject = true;

                                        // 看该谓语是否有双宾语
                                        for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                            if (se.getRelation().toString().equals("iobj") &&
                                                    se.getGovernor().index() == predicateIndex) {
                                                isDoubleObject = true;
                                                break;
                                            }
                                        }
                                        if (hasObject && !isDoubleObject) {
                                            System.out.println("主谓宾：" + subject + " " + predicate + " " + object);
                                        }
                                    }
                                }
                            } // if (semanticGraphEdge.getRelation().toString().equals("dobj"))
                        } // for

                        // 判断是否为主谓 + 补语的形态
                        if (!hasObject && !isDoubleObject) {
                            String complement = "";
                            int startIndex = 0;
                            // 查找xcomp依存关系，找到第一个补语
                            for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                if (se.getRelation().toString().equals("xcomp") &&
                                        se.getGovernor().index() == predicateIndex) {
                                    complement = se.getDependent().word();
                                    startIndex = se.getDependent().index();
                                }
                            }
                            complement = getComplement(startIndex, complement, dependency);
                            System.out.println("主谓+短语：" + subject + " " + predicate + " " + complement);
                        } // if 主谓 + 补语
                    }
                    break;

                case "cop":
                    // 匹配是否包含主系表结构
                    String copula = edge.getDependent().word();
                    Edge temp = getRealEdge(edge.getGovernor().index(), dependency);
                    String predicative = (temp == null ? edge.getGovernor().word() : temp.getWord());
                    int predicativeIndex = edge.getGovernor().index();
                    // 寻找nsubj依存关系，找出系动词的主语
                    for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                        if (semanticGraphEdge.getRelation().toString().equals("nsubj")) {
                            // 通过单词位置判断是否是同一个表语
                            if (semanticGraphEdge.getGovernor().index() == predicativeIndex) {
                                temp =  getRealEdge(semanticGraphEdge.getDependent().index(), dependency);
                                String subject = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());
                                System.out.println("主系表：" + subject + " " + copula + " " + predicative);
                            }
                        }
                    }
                    break;

                case "xcomp":
                    // 匹配是否为双宾语结构
                    if (govAndDep.matches("(VB[A-Z]{0,1})-(NN[A-Z]{0,1})") &&
                            CorpusConstant.DOUBLE_OBJECT_VERB_SET.contains(edge.getGovernor().lemma().toLowerCase())){
                        String verb = edge.getGovernor().word();
                        int verbIndex = edge.getGovernor().index();
                        int directObjectIndex = edge.getDependent().index();
                        temp = getRealEdge(directObjectIndex, dependency);
                        String directObject = (temp == null ? edge.getDependent().word() : temp.getWord());
                        // 寻找nsubj依存关系，找出主语
                        for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                            if (semanticGraphEdge.getRelation().toString().equals("nsubj") &&
                                    semanticGraphEdge.getGovernor().index() == verbIndex) {
                                temp = getRealEdge(semanticGraphEdge.getDependent().index(), dependency);
                                String subject = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());

                                // 寻找nsubj依存关系，找出间接宾语
                                for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                    if (se.getRelation().toString().equals("nsubj") &&
                                            se.getGovernor().index() == directObjectIndex) {
                                        temp = getRealEdge(se.getDependent().index(), dependency);
                                        String inderectObject = (temp == null ? se.getDependent().word() : temp.getWord());
                                        System.out.println("双宾语：" + subject + " " + verb + " " + inderectObject + " " + directObject);
                                    }
                                }
                            }
                        }
                    }

                    // 匹配是否为其他主系表结构，系动词主要是一些become，get，look等动词
                    else if (edge.getGovernor().tag().matches("VB[A-Z]{0,1}") &&
                            CorpusConstant.COPULA_LEMMA_SET.contains(edge.getGovernor().lemma().toLowerCase())) {

                        copula = edge.getGovernor().word();
                        int copulaIndex = edge.getGovernor().index();
                        temp = getRealEdge(edge.getDependent().index(), dependency);
                        predicative = (temp == null ? edge.getDependent().word() : temp.getWord());
                        predicativeIndex =  (temp == null ? edge.getDependent().index() : temp.getIndex());
                        String complement = "";
                        int startIndex = predicativeIndex;
                        complement = getComplement(startIndex, "", dependency);

                        // 寻找nsubj依存关系，找出系动词的主语
                        for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                            // 通过单词位置判断是否是同一个系动词
                            if (semanticGraphEdge.getRelation().toString().equals("nsubj") &&
                                    semanticGraphEdge.getGovernor().index() == copulaIndex) {
                                temp = getRealEdge(semanticGraphEdge.getDependent().index(), dependency);
                                String subject = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());
                                System.out.println("主系表：" + subject + " " + copula + " " + predicative + " " + complement);
                            }
                        }
                    }
                    // 匹配是否为主谓宾+宾补的结构
                    else {
                        String verb = edge.getGovernor().word();
                        int verbIndex = edge.getGovernor().index();
                        String complement = edge.getDependent().word();
                        int complementIndex = edge.getDependent().index();
                        // 寻找nsubj依存关系，找出主语
                        for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                            if (semanticGraphEdge.getRelation().toString().equals("nsubj") &&
                                    semanticGraphEdge.getGovernor().index() == verbIndex) {
                                temp = getRealEdge(semanticGraphEdge.getDependent().index(), dependency);
                                String subject = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());

                                // 寻找nsubj依存关系，找出宾语
                                for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                    if (se.getRelation().toString().equals("nsubj") &&
                                            se.getGovernor().index() == complementIndex) {
                                        temp = getRealEdge(se.getDependent().index(), dependency);
                                        String object = (temp == null ? se.getDependent().word() : temp.getWord());
                                        System.out.println("主谓宾+宾补：" + subject + " " + verb + " " + object + " " + complement);
                                    }
                                }
                            }
                        }

                    }
                    break;

                case "iobj":
                    // 匹配是否为双宾语句
                    String subject = null;
                    String verb = edge.getGovernor().word();
                    int verbIndex = edge.getGovernor().index();
                    temp = getRealEdge(edge.getDependent().index(), dependency);
                    String indirectObject = (temp == null ? edge.getDependent().word() : temp.getWord());
                    String directObject = null;
                    // 寻找nsubj依存关系，找出主语
                    for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                        if (semanticGraphEdge.getRelation().toString().equals("nsubj") &&
                                semanticGraphEdge.getGovernor().index() == verbIndex) {
                            temp = getRealEdge(semanticGraphEdge.getDependent().index(), dependency);
                            subject = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());
                            // 寻找dobj关系，找出直接宾语
                            for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                if (se.getRelation().toString().equals("dobj") &&
                                        se.getGovernor().index() == verbIndex) {
                                    temp = getRealEdge(se.getDependent().index(), dependency);
                                    directObject = (temp == null ? se.getDependent().word() : temp.getWord());
                                    System.out.println("双宾语" + subject + " " + verb + " " + indirectObject + " " + directObject);
                                }
                            }
                        }
                    }
                    break;

                case "nsubjpass":
                    // 识别被动语态
                    temp = getRealEdge(edge.getDependent().index(), dependency);
                    subject = (temp == null ? edge.getDependent().word() : temp.getWord());
                    String passiveVerb = edge.getGovernor().word();
                    int passiveVerbIndex = edge.getGovernor().index();

                    // 寻找auxpass依存关系，找出be
                    for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                        if (semanticGraphEdge.getRelation().toString().equals("auxpass") &&
                        semanticGraphEdge.getDependent().lemma().equals("be") &&
                        semanticGraphEdge.getGovernor().index() == passiveVerbIndex) {
                            String be = semanticGraphEdge.getDependent().word();

                            boolean hasAgent = false;
                            // 寻找nmod:agent依存关系，找出施事者
                            for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                if (se.getRelation().toString().equals("nmod:agent") &&
                                se.getGovernor().index() == passiveVerbIndex) {
                                    Edge tempEdge = getRealEdge(se.getDependent().index(), dependency);
                                    String agent = (tempEdge == null ? se.getDependent().word() : tempEdge.getWord());
                                    int agentIndex = (tempEdge == null ? se.getDependent().index() : tempEdge.getIndex());
                                    hasAgent = true;

                                    // 寻找by
                                    for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                                        if (e.getRelation().toString().equals("case") &&
                                        e.getGovernor().index() == agentIndex &&
                                        CorpusConstant.PASSIVE_PREP_SET.contains(e.getDependent().word())) {
                                            String prep = e.getDependent().word();
                                            System.out.println("被动句：" + subject + " " + be + " " +passiveVerb + " " + prep + " " + agent);
                                        }
                                    }
                                }
                            }

                            if (!hasAgent) {
                                System.out.println("被动：" + subject + " " + be + " " +passiveVerb);
                            }
                        }
                    }
                    break;

                default:
                    break;
            }
        }

    }

    /**
     * 获取真正的主语、宾语等词（因为会有a tape of, a box of等修饰名词的情况）
     *
     * @param index
     * @param dependency
     * @return
     */
    private static Edge getRealEdge(int index, SemanticGraph dependency) {
        Edge result = new Edge();
        // 查找nmod:of关系
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            if (edge.getRelation().toString().equals("nmod:of") &&
                    edge.getGovernor().index() == index) {
                if (CorpusConstant.PARTITIVE_NOUN_SET.contains(edge.getDependent().lemma()) &&
                    edge.getDependent().tag().matches("NN.*")) {
                    result.setWord(edge.getDependent().word());
                    result.setIndex(edge.getDependent().index());
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * 补全补语
     *
     * @param startIndex        补语第一个单词的起始位置
     * @param startComplement   第一个补语单词
     * @param dependency
     * @return
     */
    private static String getComplement(int startIndex, String startComplement, SemanticGraph dependency) {
        String complement = startComplement;
        // 循环查找，看是否还有补语
        boolean hasComplement = true;
        while (hasComplement) {
            hasComplement = false;
            for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                if (semanticGraphEdge.getRelation().toString().equals("xcomp") &&
                        semanticGraphEdge.getGovernor().index() == startIndex) {
                    complement = complement + " " + semanticGraphEdge.getDependent().word();
                    startIndex = semanticGraphEdge.getDependent().index();
                    hasComplement = true;
                } else if (semanticGraphEdge.getRelation().toString().equals("dobj") &&
                        semanticGraphEdge.getGovernor().index() == startIndex){
                    complement = complement + " " + semanticGraphEdge.getDependent().word();
                    hasComplement = false;
                }
            }
        } // while (hasComplement)

        return complement;
    }


    public static void main(String[] args) {
        String text = "The new accusations against China made by the United States in the update of the Section 301 investigation disregard the facts and are totally unacceptable.";
        List<CoreMap> result = StanfordParserUtil.parse(text);

        for(CoreMap sentence : result) {
//            System.out.println(sentence.get(TreeCoreAnnotations.TreeAnnotation.class));
            matchPhrase(sentence);
        }
    }
}
