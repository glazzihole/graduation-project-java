package com.hugailei.graduation.corpus.util;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.domain.SentencePattern;
import com.hugailei.graduation.corpus.enums.SentencePatternType;
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

import java.util.*;

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
     * 找出句子中的所有特殊句型
     *
     * @param sentence
     * @return
     */
    public static List<SentencePattern> findAllSpecialSentencePattern(CoreMap sentence) {
        List<SentencePattern> sentencePatternList = new ArrayList<>();
        List<SentencePattern> tempList = matchSubjectClause(sentence);

        if (tempList != null) {
            sentencePatternList.addAll(tempList);
        }

        tempList = matchObjectClauseOrPredicativeClause(sentence);
        if (tempList != null) {
            sentencePatternList.addAll(tempList);
        }

        tempList = matchAppositiveClauseOrAttributiveClause(sentence);
        if (tempList != null) {
            sentencePatternList.addAll(tempList);
        }

        tempList = matchAdverbialClause(sentence);
        if (tempList != null) {
            sentencePatternList.addAll(tempList);
        }

        tempList = matchDoubleObject(sentence);
        if (tempList != null) {
            sentencePatternList.addAll(tempList);
        }

        tempList = matchPassiveVoice(sentence);
        if (tempList != null) {
            sentencePatternList.addAll(tempList);
        }

        if (SentencePatternUtil.hasSoThat(sentence)) {
            int type = SentencePatternType.S_THAT.getType();
            SentencePattern sentencePattern = new SentencePattern();
            sentencePattern.setType(type);
            sentencePatternList.add(sentencePattern);
        }

        if (SentencePatternUtil.hasTooTo(sentence)) {
            int type = SentencePatternType.TOO_TO.getType();
            SentencePattern sentencePattern = new SentencePattern();
            sentencePattern.setType(type);
            sentencePatternList.add(sentencePattern);
        }

        if (SentencePatternUtil.hasInvertedStructure(sentence)) {
            int type = SentencePatternType.INVERTED_STRUCTURE.getType();
            SentencePattern sentencePattern = new SentencePattern();
            sentencePattern.setType(type);
            sentencePatternList.add(sentencePattern);
        }

        if (SentencePatternUtil.hasEmphaticStructure(sentence)) {
            int type = SentencePatternType.EMPHATIC_STRUCTURE.getType();
            SentencePattern sentencePattern = new SentencePattern();
            sentencePattern.setType(type);
            sentencePatternList.add(sentencePattern);
        }

        if (sentencePatternList == null) {
            return null;
        }
        return sentencePatternList;
    }

    /**
     * 匹配句法树中的主语从句
     *
     * @param sentence 句法分析结果
     */
    public static List<SentencePattern> matchSubjectClause(CoreMap sentence) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        TregexPattern pattern = TregexPattern.compile("SBAR $+ VP");
        TregexMatcher matcher = pattern.matcher(tree);
        LinkedHashSet<String> clauseSet = new LinkedHashSet<>();
        // 匹配输出
        while (matcher.findNextMatchingNode()) {
            int type = SentencePatternType.SUBJECT_CLAUSE.getType();
            StringBuilder clauseContent = new StringBuilder();

            // 匹配SBAR，得出从句内容
            Tree matchTree = matcher.getMatch();
            TregexMatcher clauseMatcher = TregexPattern.compile("SBAR == SBAR").matcher(matchTree);
            if (clauseMatcher.findNextMatchingNode()) {
                for (Tree children : clauseMatcher.getMatch().getLeaves()) {
                    clauseContent.append(children.label().value()).append(" ");
                }
                clauseSet.add(type + "_" + clauseContent.toString());
            }
        }
        return setToList(clauseSet);
    }

    /**
     * 匹配句法树中的宾语从句或者表语从句
     *
     * @param sentence 句法分析结果
     */
    public static List<SentencePattern> matchObjectClauseOrPredicativeClause(CoreMap sentence) {
        List<SentencePattern> adverbialClauseList = matchAdverbialClause(sentence);
        LinkedHashSet<String> clauseSet = new LinkedHashSet();

        // 先判断句中有没有so that句，若有，则需排除that后的从句
        Set<Integer> soThatIndexSet = getSoThatClauseIndex(sentence);
        // 排除掉it is XX that强调句的句型
        Set<Integer> emphaticIndexSet = getEmphaticStructure(sentence);
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        TregexPattern pattern = TregexPattern.compile("/^P.*$/ << SBAR");
        TregexMatcher matcher = pattern.matcher(tree);
        while (matcher.findNextMatchingNode()) {
            // 匹配SBAR，得出从句内容
            TregexMatcher clauseMatcher = TregexPattern.compile("SBAR == SBAR").matcher(matcher.getMatch());
            StringBuilder clauseContent = new StringBuilder();
            if (clauseMatcher.findNextMatchingNode()) {
                int clauseStartIndex = clauseMatcher.getMatch().getLeaves().get(0).indexLeaves(0, false) - 2;
                if (soThatIndexSet != null && soThatIndexSet.contains(clauseStartIndex)) {
                    continue;
                } else if (emphaticIndexSet != null && emphaticIndexSet.contains(clauseStartIndex)) {
                    continue;
                }
                for (Tree leaf : clauseMatcher.getMatch().getLeaves()) {
                    clauseContent.append(leaf.label().value()).append(" ");
                }
                clauseSet.add(SentencePatternType.OBJECT_CLAUSE.getType() + "_" + clauseContent.toString());
            }
        }
        pattern = TregexPattern.compile("VP << (/^VB.*$/ [$+ SBAR | $+ S])");
        matcher = pattern.matcher(tree);
        while (matcher.findNextMatchingNode()) {
            Tree match = matcher.getMatch();
            // 排除从句为so that 句型和it is that的强调句型
            TregexMatcher clauseMatcher = TregexPattern.compile("SBAR == SBAR").matcher(matcher.getMatch());
            if (clauseMatcher.findNextMatchingNode()) {
                int clauseStartIndex = clauseMatcher.getMatch().getLeaves().get(0).indexLeaves(0, false) - 2;
                if (soThatIndexSet != null && soThatIndexSet.contains(clauseStartIndex)) {
                    continue;
                } else if (emphaticIndexSet != null && emphaticIndexSet.contains(clauseStartIndex)) {
                    continue;
                }
            }

            boolean found = false;
            //是否为表语从句的标识
            boolean isPredicativeClause = false;
            // 获取匹配到的树结构的子节点
            Tree[] childrens = match.children();
            // 获取动词
            String verbReg = "VB.*";
            for (Tree children : childrens) {
                if (children.label().toString().matches(verbReg)) {
                    // 获取当前单词在句子中是第几个单词（从0开始）
                    int index = children.getLeaves().get(0).indexLeaves(0, false) - 2;
                    // 根据index获取动词的原型
                    CoreLabel coreLabel = sentence.get(CoreAnnotations.TokensAnnotation.class).get(index);
                    String lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);

                    // 查找句法依存关系，看该动词是否有补语(ccomp)
                    for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
                        if (edge.getRelation().toString().equals("ccomp") ||
                            edge.getRelation().toString().equals("dep")) {
                            if(edge.getGovernor().index() - 1 == index) {
                                if (edge.getDependent().index() - 1 > index) {
                                    found = true;
                                    // 判断该动词是否为系统词
                                    if (CorpusConstant.COPULA_LEMMA_SET.contains(lemma)) {
                                        isPredicativeClause = true;
                                    }
                                    break;
                                }
                            }
                        } else if (edge.getRelation().toString().startsWith("advcl")) {
                            if(edge.getGovernor().index() - 1 == index) {
                                if (edge.getDependent().index() - 1 > index) {
                                    if (CorpusConstant.COPULA_LEMMA_SET.contains(lemma)) {
                                        isPredicativeClause = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (found || isPredicativeClause) {
                        // 匹配SBAR，得出从句内容
                        clauseMatcher = TregexPattern.compile("SBAR == SBAR").matcher(match);
                        StringBuilder clauseContent = new StringBuilder();
                        if (clauseMatcher.findNextMatchingNode()) {
                            for (Tree leaf : clauseMatcher.getMatch().getLeaves()) {
                                clauseContent.append(leaf.label().value()).append(" ");
                            }
                        }
                        if (isPredicativeClause) {
                            // 需要排除状语从句 如I did not realize how special my mother was until ……这类句子，则会被识别成表语从句
                            boolean isAdverbialClause = false;
                            for (SentencePattern sp : adverbialClauseList) {
                                String advClauseContent = sp.getClauseContent();
                                if (clauseContent.toString().equals(advClauseContent)) {
                                    isAdverbialClause = true;
                                    break;
                                }
                            }
                            if (!isAdverbialClause) {
                                clauseSet.add(SentencePatternType.OBJECT_CLAUSE.getType() + "_" + clauseContent.toString());
                            }
                        } else {
                            clauseSet.add(SentencePatternType.OBJECT_CLAUSE.getType() + "_" + clauseContent.toString());
                        }
                        break;
                    } // if (found || isPredicativeClause)
                }
            } // for (Tree children : childrens)
        } // while

        return setToList(clauseSet);
    }

    /**
     * 匹配句法树中的同位语从句或定语从句
     *
     * @param sentence 句法分析结果
     */
    public static List<SentencePattern> matchAppositiveClauseOrAttributiveClause(CoreMap sentence) {
        // 先判断句中有没有so that句，若有，则需排除that后的从句，否则会识别为定语从句。
        Set<Integer> thatIndexSet = getSoThatClauseIndex(sentence);
        // 排除掉it is XX that强调句的句型
        Set<Integer> emphaticIndexSet = getEmphaticStructure(sentence);

        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        TregexPattern pattern = TregexPattern.compile("NP $.. SBAR");
        TregexMatcher matcher = pattern.matcher(tree);
        List<SentencePattern> sentencePatternList = new ArrayList<>();
        while (matcher.findNextMatchingNode()) {
            // 是否找到
            boolean found = false;
            Tree match = matcher.getMatch();
            Tree[] childrens = match.children();
            // 获取从句所修饰的名词
            String nounReg = "NN.*";
            for (Tree children : childrens) {
                if (children.label().toString().matches(nounReg)) {
                    // 获取该名词的index，从0开始
                    int index = children.getLeaves().get(0).indexLeaves(0, false) - 2;
                    // 通过index获取名词的原型
                    String lemma = sentence.get(CoreAnnotations.TokensAnnotation.class).get(index).get(CoreAnnotations.LemmaAnnotation.class);
                    // 判断该名词是否为同位语从句的先行词
                    if (CorpusConstant.APPOSITIVE_ANTECEDENT_SET.contains(lemma)) {
                        found = true;
                    } else {
                        // 查找句法依存关系，看该名词是否有定语从句(acl)或者从句补语ccomp
                        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
                            if (edge.getRelation().toString().equals("ccomp") ||
                                edge.getRelation().toString().equals("dep") ||
                                edge.getRelation().toString().startsWith("acl")) {
                                if (edge.getGovernor().index() - 1 == index) {
                                    if (edge.getDependent().index() - 1 > index) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        } // for (SemanticGraphEdge edge : dependency.edgeListSorted())
                    }
                    if (found) {
                        int type;
                        type = SentencePatternType.ATTRIBUTIVE_CLAUSE_OR_APPOSITIVE_CLAUSE.getType();
                        // 匹配SBAR，得出从句内容
                        Tree parent = match.parent(tree);
                        TregexMatcher clauseMatcher = TregexPattern.compile("SBAR == SBAR").matcher(parent);
                        StringBuilder clauseContent = new StringBuilder();
                        if (clauseMatcher.findNextMatchingNode()) {
                            int clauseStartIndex = clauseMatcher.getMatch().getLeaves().get(0).indexLeaves(0, false) - 2;
                            if (thatIndexSet != null && thatIndexSet.contains(clauseStartIndex)) {
                                continue;
                            } else if (emphaticIndexSet != null && emphaticIndexSet.contains(clauseStartIndex)) {
                                continue;
                            }
                            for (Tree leaf : clauseMatcher.getMatch().getLeaves()) {
                                clauseContent.append(leaf.label().value()).append(" ");
                            }
                            SentencePattern result = new SentencePattern(type, null, null, clauseContent.toString(), null, null, null, null);

                            // 查找被修饰的词
                            List<SentencePattern> tempList = matchModificand(clauseMatcher.getMatch(), sentence, "NP");
                            if (tempList != null) {
                                result.setModificand(tempList.get(0).getModificand());
                                result.setModificandPos(StanfordParserUtil.getBasePos(tempList.get(0).getModificandPos()));
                            }
                            sentencePatternList.add(result);
                        }
                        break;
                    } // if (found || isAppositiveClause)
                } // if (children.label().toString().matches(nounReg))
            }// for (Tree children : childrens)

        } // while

        if (sentencePatternList.isEmpty()) {
            return null;
        }
        return sentencePatternList;
    }

    /**
     * 匹配状语从句
     *
     * @param sentence 句法分析结果
     */
    public static List<SentencePattern> matchAdverbialClause(CoreMap sentence) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        // 判断依存关系中是否包含advcl:XXX
        LinkedHashSet<String> clauseSet = new LinkedHashSet<>();
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            String relation = edge.getRelation().toString();
            if (CorpusConstant.ADVERBIAL_CLAUSE_RELATION_SET.contains(relation)) {
                // 引导词
                String conjunction = relation.split(":")[1];
                // 匹配SBAR，看从句的第一个词是否为状语从句引导词，若是，则匹配从句内容
                TregexMatcher clauseMatcher = TregexPattern.compile("SBAR == SBAR").matcher(tree);
                while (clauseMatcher.findNextMatchingNode()) {
                    StringBuilder clauseContent = new StringBuilder();
                    boolean found = false;
                    if (conjunction.contains("_")) {
                        if (clauseMatcher.getMatch().getLeaves().get(0).label().value().equals(conjunction.split("_")[0]) &&
                            clauseMatcher.getMatch().getLeaves().get(1).label().value().equals(conjunction.split("_")[1])) {
                            for (Tree children : clauseMatcher.getMatch().getLeaves()) {
                                clauseContent.append(children.label().value()).append(" ");
                            }
                            found = true;
                        }
                    } else if (clauseMatcher.getMatch().getLeaves().get(0).label().value().equals(conjunction)) {
                        for (Tree children : clauseMatcher.getMatch().getLeaves()) {
                            clauseContent.append(children.label().value()).append(" ");
                        }
                        found = true;
                    }
                    if (found) {
                        clauseSet.add(SentencePatternType.ADVERBIAL_CLAUSE.getType() + "_" + clauseContent.toString());
                        break;
                    }
                }
            }
            else if (relation.equals("advcl")) {
                int tempIndex = edge.getDependent().index();
                // 循环查找wh-引导的从句
                for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                    if (se.getRelation().toString().equals("advmod") &&
                        se.getGovernor().index() == tempIndex){
                        String wh = se.getDependent().lemma();
                        if (CorpusConstant.ADVERBIAL_CLAUSE_CONJECTION_SET.contains(wh)) {
                            StringBuilder clauseContent = new StringBuilder();
                            // 匹配SBAR，看从句的第一个词是否为状语从句引导词，若是，则匹配从句内容
                            TregexMatcher clauseMatcher = TregexPattern.compile("SBAR == SBAR").matcher(tree);
                            while (clauseMatcher.findNextMatchingNode()) {
                                if ((clauseMatcher.getMatch().getLeaves().get(0).label().value()).equals(wh)) {
                                    for (Tree children : clauseMatcher.getMatch().getLeaves()) {
                                        clauseContent.append(children.label().value()).append(" ");
                                    }
                                    break;
                                }
                            }
                            clauseSet.add(SentencePatternType.ADVERBIAL_CLAUSE.getType() + "_" + clauseContent.toString());
                            break;
                        }
                    }
                }
            }
        }

        return setToList(clauseSet);
    }

    /**
     * 匹配被动语态
     *
     * @param sentence 句法分析结果
     */
    public static List<SentencePattern> matchPassiveVoice(CoreMap sentence) {
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        List<SentencePattern> sentencePatternList = new ArrayList<>();
        // 判断依存关系中是否包含nsubjpass
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            String relation = edge.getRelation().toString();
            if (relation.startsWith("nsubjpass")) {
                int type = SentencePatternType.PASSIVE_VOICE.getType();
                int passiveVerbIndex = edge.getGovernor().index();
                int subjectIndex = edge.getDependent().index();
                Edge temp = getRealNounEdge(subjectIndex, dependency);
                subjectIndex = (temp == null ? subjectIndex : temp.getIndex());
                boolean hasAgent = false;
                // 寻找nmod:agent依存关系，找出施事者
                for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                    if (semanticGraphEdge.getRelation().toString().equals("nmod:agent") &&
                        semanticGraphEdge.getGovernor().index() == passiveVerbIndex) {
                        Edge tempEdge = getRealNounEdge(semanticGraphEdge.getDependent().index(), dependency);
                        int agentIndex = (tempEdge == null ? semanticGraphEdge.getDependent().index() : tempEdge.getIndex());
                        hasAgent = true;
                        sentencePatternList.add(new SentencePattern(type, null, null, null, subjectIndex - 1, null, null, agentIndex - 1));
                    }
                }

                if (!hasAgent) {
                    sentencePatternList.add(new SentencePattern(type, null, null, null, subjectIndex - 1, null, null, null));
                }
            }
        }
        if (sentencePatternList.isEmpty()) {
            return null;
        }
        return sentencePatternList;
    }

    /**
     * 匹配双宾语句
     *
     * @param sentence 句法分析结果 句法分析结果
     */
    public static List<SentencePattern> matchDoubleObject(CoreMap sentence) {
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        List<SentencePattern> sentencePatternList = new ArrayList<>();
        int type = SentencePatternType.DOUBLE_OBJECT.getType();
        // 判断依存关系中是否包含iobj(间接宾语)
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            String relation = edge.getRelation().toString();
            if (relation.startsWith("iobj")) {
                int subjectIndex, directObjectIndex;
                int verbIndex = edge.getGovernor().index();
                Edge temp = getRealNounEdge(edge.getDependent().index(), dependency);
                int indirectObjectIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());

                // 寻找nsubj依存关系，找出主语
                for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                    if (semanticGraphEdge.getRelation().toString().equals("nsubj") &&
                            semanticGraphEdge.getGovernor().index() == verbIndex) {
                        temp = getRealNounEdge(semanticGraphEdge.getDependent().index(), dependency);
                        subjectIndex = (temp == null ? semanticGraphEdge.getDependent().index() : temp.getIndex());
                        // 寻找dobj关系，找出直接宾语
                        for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                            if (se.getRelation().toString().equals("dobj") &&
                                    se.getGovernor().index() == verbIndex) {
                                temp = getRealNounEdge(se.getDependent().index(), dependency);
                                directObjectIndex = (temp == null ? se.getDependent().index() : temp.getIndex());
                                sentencePatternList.add(new SentencePattern(type, null, null, null, subjectIndex - 1, directObjectIndex - 1, indirectObjectIndex - 1, null));
                            }
                        }
                    }
                }
            }
            // 可能存在无法直接通过iobj识别的情况，可再根据动词等情况进一步判断
            else {
                String govAndDep = edge.getGovernor().tag() + "-" + edge.getDependent().tag();
                if (govAndDep.matches("(VB[A-Z]{0,1})-(NN[A-Z]{0,1})") &&
                        CorpusConstant.DOUBLE_OBJECT_VERB_SET.contains(edge.getGovernor().lemma().toLowerCase())){
                    int verbIndex = edge.getGovernor().index();
                    int directObjectIndex = edge.getDependent().index();
                    Edge temp = getRealNounEdge(directObjectIndex, dependency);
                    directObjectIndex = (temp == null ? directObjectIndex : temp.getIndex());
                    // 寻找nsubj依存关系，找出主语
                    for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                        if ((semanticGraphEdge.getRelation().toString().equals("nsubj") ||
                             semanticGraphEdge.getRelation().toString().equals("discourse"))&&
                             semanticGraphEdge.getGovernor().index() == verbIndex) {

                            // 寻找nsubj依存关系，找出间接宾语
                            for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                if (se.getRelation().toString().equals("nsubj") &&
                                    se.getGovernor().index() == directObjectIndex) {
                                    int subjectIndex = semanticGraphEdge.getDependent().index();
                                    temp = getRealNounEdge(subjectIndex, dependency);
                                    subjectIndex = (temp == null ? subjectIndex : temp.getIndex());
                                    int indirectObjectIndex = se.getDependent().index();
                                    temp = getRealNounEdge(indirectObjectIndex, dependency);
                                    indirectObjectIndex = (temp == null ? indirectObjectIndex : temp.getIndex());
                                    sentencePatternList.add(new SentencePattern(type, null, null, null, subjectIndex - 1, directObjectIndex - 1, indirectObjectIndex - 1, null));
                                }
                            }
                        }
                    }
                }
            }
        } // for (SemanticGraphEdge edge : dependency.edgeListSorted())

        if (sentencePatternList.isEmpty()) {
            return null;
        }
        return sentencePatternList;
    }

    /**
     * 匹配是否包含so that/such that句型
     *
     * @param sentence 句法分析结果 句法分析结果
     */
    public static boolean hasSoThat(CoreMap sentence) {
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        // 找so或such 及其所修饰的词
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            if (edge.getDependent().word().toLowerCase().equals("so") ||
                edge.getDependent().word().toLowerCase().equals("such")) {
                int afterSoIndex = edge.getGovernor().index();

                // 查找that所修饰的词
                for (SemanticGraphEdge s : dependency.edgeListSorted()) {
                    if (s.getRelation().toString().startsWith("mark") &&
                        s.getDependent().word().toLowerCase().equals("that") &&
                        s.getDependent().tag().equals("IN")) {
                        int afterThatIndex = s.getGovernor().index();
                        // 看so所修饰的词和that所修饰的词之间是否存在依存关系
                        for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                            if ((e.getRelation().toString().startsWith("dep") || e.getRelation().toString().startsWith("ccomp") &&
                                 e.getGovernor().index() == afterSoIndex &&
                                 e.getDependent().index() == afterThatIndex)) {
                                return true;
                            }
                        }

                        // 若so所修饰的词和that所修饰的词之间不存在直接的依存关系，则先查找与that所修饰的词存在依存关系的词A
                        for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                            if (e.getDependent().index() == afterThatIndex &&
                                (e.getRelation().toString().startsWith("cop") ||
                                 e.getRelation().toString().startsWith("ccomp") ||
                                 e.getRelation().toString().startsWith("dep") )) {
                                int tempAIndex = e.getGovernor().index();
                                // 查找A与so所修饰的词是否存在指定关系
                                for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                    if (se.getDependent().index() == afterSoIndex &&
                                        se.getGovernor().index() == tempAIndex) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 匹配是否包含so that/such that句型，返回that的所有位置（从0开始计数）
     *
     * @param sentence 句法分析结果
     */
    public static Set<Integer> getSoThatClauseIndex(CoreMap sentence) {
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        Set<Integer> indexSet = new HashSet<>();
        // 找so或such 及其所修饰的词
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            boolean found = false;
            if (edge.getDependent().word().toLowerCase().equals("so") ||
                edge.getDependent().word().toLowerCase().equals("such")) {
                int afterSoIndex = edge.getGovernor().index();
                // 查找that所修饰的词
                for (SemanticGraphEdge s : dependency.edgeListSorted()) {
                    if (s.getRelation().toString().startsWith("mark") &&
                        s.getDependent().word().toLowerCase().equals("that") &&
                        s.getDependent().tag().equals("IN")) {
                        int afterThatIndex = s.getGovernor().index();
                        int thatIndex = s.getDependent().index();
                        // 看so所修饰的词和that所修饰的词之间是否存在依存关系
                        for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                            if ((e.getRelation().toString().startsWith("dep") || e.getRelation().toString().startsWith("ccomp") &&
                                e.getGovernor().index() == afterSoIndex &&
                                e.getDependent().index() == afterThatIndex)) {
                                indexSet.add(thatIndex - 1);
                            }
                        }
                        if (!found) {
                            // 若so所修饰的词和that所修饰的词之间不存在直接的依存关系，则先查找与that所修饰的词存在依存关系的词A
                            for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                                if (e.getDependent().index() == afterThatIndex &&
                                        (e.getRelation().toString().startsWith("cop") ||
                                        e.getRelation().toString().startsWith("ccomp") ||
                                        e.getRelation().toString().startsWith("dep") )) {
                                    int tempAIndex = e.getGovernor().index();
                                    // 查找A与so所修饰的词是否存在指定关系
                                    for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                        if (se.getDependent().index() == afterSoIndex &&
                                                se.getGovernor().index() == tempAIndex) {
                                            indexSet.add(thatIndex - 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (indexSet.isEmpty()) {
            return null;
        }
        return indexSet;
    }

    /**
     * 是否包含 too to句型
     *
     * @param sentence
     * @return
     */
    public static boolean hasTooTo (CoreMap sentence) {
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        // 找too及其所修饰的词
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            boolean found = false;
            if (edge.getDependent().word().equals("too")) {
                int afterTooIndex = edge.getGovernor().index();

                // 查找to所修饰的词
                for (SemanticGraphEdge s : dependency.edgeListSorted()) {
                    if (s.getRelation().toString().startsWith("mark") &&
                        s.getDependent().word().equals("to")) {
                        int afterToIndex = s.getGovernor().index();
                        // 看so所修饰的词和that所修饰的词之间是否存在依存关系
                        for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                            if ((e.getRelation().toString().startsWith("dep") || e.getRelation().toString().startsWith("xcomp") &&
                                 e.getGovernor().index() == afterTooIndex &&
                                 e.getDependent().index() == afterToIndex)) {
                               return true;
                            }
                        }
                        if (!found) {
                            // 若too所修饰的词和to所修饰的词之间不存在直接的依存关系，则先查找与to所修饰的词存在依存关系的词A
                            for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                                if (e.getDependent().index() == afterToIndex &&
                                    e.getRelation().toString().startsWith("xcomp")) {
                                    int tempAIndex = e.getGovernor().index();
                                    // 查找A与so所修饰的词是否存在指定关系
                                    for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                        if (se.getDependent().index() == afterToIndex &&
                                            se.getGovernor().index() == tempAIndex) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 匹配是否包含倒装结构
     *
     * @param sentence
     * @return
     */
    public static boolean hasInvertedStructure(CoreMap sentence) {
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        // 判断句子是否是疑问句
        int sentenceWordCount = sentence.get(CoreAnnotations.TokensAnnotation.class).size();
        String punct = sentence.get(CoreAnnotations.TokensAnnotation.class).get(sentenceWordCount - 1).lemma();
        boolean isQuestion = false;
        if (punct.equals("?")) {
            isQuestion = true;
        }
        // 判断句子中是否包含情态动词 + 代词 + 动词的结构
        StringBuilder posString = new StringBuilder();
        StringBuilder lemmaString = new StringBuilder();
        for (CoreLabel label : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
            posString.append(StanfordParserUtil.getBasePos(label.tag())).append(" ");
            lemmaString.append(label.lemma()).append(" ");
        }
        if (posString.toString().contains(" MD PRP VB") && !isQuestion) {
            return true;
        }
        // 判断是否包含 neither do I 这样的结构
        for (String structure : CorpusConstant.INVERTED_STRUCTURE_SET) {
            if (lemmaString.toString().contains(structure)) {
                return true;
            }
        }
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            if (edge.getRelation().toString().equals("advmod")) {
                String posPair = edge.getGovernor() + "-" + edge.getDependent();
                if (posPair.matches("(VB[A-Z]{0,1})-(RB[A-Z]{0,1})")) {
                    if (edge.getDependent().index() < edge.getGovernor().index()) {
                        // 判断副词后跟的是as或though
                        String word = sentence.get(CoreAnnotations.TokensAnnotation.class).get(edge.getDependent().index()).lemma();
                        if (word.equals("as") || word.equals("though")) {
                            return true;
                        }
                    }
                }
            }
            if (edge.getRelation().toString().equals("nsubj")) {
                String posPair = edge.getGovernor() + "-" + edge.getDependent();
                // here comes the bus
                if (posPair.matches("(VB[A-Z]{0,1})-(NN[A-Z]{0,1})") &&
                    edge.getGovernor().index() < edge.getDependent().index()) {
                    return true;
                }
                // away went the crowd
                else if (posPair.matches("(VB[A-Z]{0,1})-(RB[A-Z]{0,1})") &&
                           edge.getDependent().index() < edge.getGovernor().index()) {
                    return true;
                }
                if (edge.getGovernor().tag().startsWith("VB")  &&
                    (edge.getDependent().tag().matches("NN.*") || edge.getDependent().tag().equals("PRP"))) {
                    int verbIndex = edge.getGovernor().index();
                    // 看情态动词是否在主语之前
                    for (SemanticGraphEdge s : dependency.edgeListSorted()) {
                        if (s.getGovernor().index() == verbIndex &&
                            s.getDependent().tag().equals("MD")) {
                            if (s.getDependent().index() < edge.getDependent().index() && !isQuestion) {
                                return true;
                            }
                        }
                    }
                    // well do I remember the day, many a time have I seen her
                    for (SemanticGraphEdge s : dependency.edgeListSorted()) {
                        if (s.getRelation().toString().equals("ccomp") && s.getDependent().index() == verbIndex) {
                            if (s.getGovernor().lemma().equals("do") ||
                                s.getGovernor().lemma().equals("have") ||
                                s.getGovernor().tag().equals("MD")) {
                                if (s.getGovernor().index() < edge.getDependent().index()) {
                                    return true;
                                }
                            }
                        }
                    }
                    // the harder you work, ..
                    for (SemanticGraphEdge s : dependency.edgeListSorted()) {
                        if (s.getRelation().toString().equals("dep") && s.getGovernor().index() == verbIndex) {
                            if ((s.getDependent().tag().matches("RB.*") || s.getDependent().tag().matches("JJ.*")) &&
                                s.getDependent().index() < edge.getDependent().index() &&
                                !s.getDependent().lemma().equals("not")) {
                                return true;
                            }
                        }
                    }
                }
            }
            if (edge.getRelation().toString().equals("cop")) {
                if (edge.getGovernor().index() < edge.getDependent().index() && !isQuestion) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断句中是否有强调句句型
     *
     * @param sentence
     * @return
     */
    public static boolean hasEmphaticStructure(CoreMap sentence) {
        // 匹配句中是否有助动词do/ does或did强调谓语动词的结构
        List<CoreLabel> coreLabelList = sentence.get(CoreAnnotations.TokensAnnotation.class);
        int wordCount = coreLabelList.size();
        for (int i = 0; i <= wordCount - 2; i++) {
            CoreLabel label = coreLabelList.get(i);
            if ("do".equals(label.lemma()) && label.tag().startsWith("VB")) {
                CoreLabel nextLabel = coreLabelList.get(i + 1);
                if (nextLabel.tag().startsWith("VB")) {
                    return true;
                }
            }
        }

        // 匹配简单的强调句句型  it is XXX that/who
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            if (edge.getRelation().toString().equals("nsubj") &&
                edge.getDependent().lemma().equals("it")) {
                int emphasizedSubjectIndex = edge.getGovernor().index();
                for (SemanticGraphEdge s : dependency.edgeListSorted()) {
                    if (s.getRelation().toString().equals("cop") &&
                        s.getGovernor().index() == emphasizedSubjectIndex) {
                        for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                            if (e.getRelation().toString().equals("ref") &&
                                e.getGovernor().index() == emphasizedSubjectIndex &&
                                (e.getDependent().lemma().equals("who") || e.getDependent().lemma().equals("that"))) {
                                    return true;
                            }
                        }

                        for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                            if (e.getRelation().toString().equals("mark") &&
                                e.getDependent().lemma().equals("that")) {
                                int ccompIndex = e.getGovernor().index();
                                for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                    if (se.getRelation().toString().equals("ccomp") &&
                                        se.getGovernor().index() == emphasizedSubjectIndex &&
                                        se.getDependent().index() == ccompIndex) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 匹配it is + 从句 + that/who 形式的强调句型
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        TregexMatcher matcher = TregexPattern.compile("it .. (was >> (VP << (SBAR << (SBAR [<< that | << who]))))").matcher(tree);
        if (matcher.findNextMatchingNode()) {
            return true;
        }
        matcher = TregexPattern.compile("it .. (is >> (VP << (SBAR << (SBAR [<< that | << who]))))").matcher(tree);
        if (matcher.findNextMatchingNode()) {
            return true;
        }
        return false;
    }

    /**
     * 获取强调句中的that/who等词在句子中的位置，从0开始计数
     *
     * @param sentence
     * @return
     */
    public static Set<Integer> getEmphaticStructure(CoreMap sentence) {
        Set<Integer> indexSet = new HashSet<>();

        // 匹配简单的强调句句型  it is XXX that/who
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            if (edge.getRelation().toString().equals("nsubj") &&
                edge.getDependent().lemma().equals("it")) {
                int emphasizedSubjectIndex = edge.getGovernor().index();
                for (SemanticGraphEdge s : dependency.edgeListSorted()) {
                    if (s.getRelation().toString().equals("cop") &&
                        s.getGovernor().index() == emphasizedSubjectIndex) {
                        for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                            if (e.getRelation().toString().equals("ref") &&
                                e.getGovernor().index() == emphasizedSubjectIndex &&
                                (e.getDependent().lemma().equals("who") || e.getDependent().lemma().equals("that"))) {
                                indexSet.add(e.getDependent().index());
                            }
                        }

                        for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                            if (e.getRelation().toString().equals("mark") &&
                                (e.getDependent().lemma().equals("that") || e.getDependent().lemma().equals("who"))) {
                                int ccompIndex = e.getGovernor().index();
                                for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                    if (se.getRelation().toString().equals("ccomp") &&
                                        se.getGovernor().index() == emphasizedSubjectIndex &&
                                        se.getDependent().index() == ccompIndex) {
                                        indexSet.add(e.getDependent().index());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 匹配it is + 从句 + that/who 形式的强调句型
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        TregexMatcher matcher = TregexPattern.compile("(was >> (VP << (SBAR << (SBAR [<< that | << who])))) ,, it").matcher(tree);
        while (matcher.findNextMatchingNode()) {
            Tree subTree = matcher.getMatch();
            Tree parentTree = subTree.parent(tree).parent(tree);
            int temp = parentTree.getLeaves().get(1).indexLeaves(0, false) - 2;
            indexSet.add(temp);
            for (Tree t : parentTree.getLeaves()) {
                if ("that".equals(t.label().value()) || "who".equals(t.label().value())) {
                    indexSet.add(t.indexLeaves(0, false) - 2);
                }
            }
        }

        if (indexSet.isEmpty()) {
            return null;
        }
        return indexSet;
    }

    /**
     * 匹配各类短语
     *
     * @param sentence 句法分析结果
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
                // 排除掉单个单词，过长的短语排除掉
                if (match.getLeaves().size() > 1 && match.getLeaves().size() <= 10) {
                    List<Edge> edgeList = new ArrayList<>();
                    for (Tree leaf : match.getLeaves()) {
                        int index = leaf.indexLeaves(0, false) - 2;
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
     * @param clauseTree    从句所在的子树  SBAR节点
     * @param sentence      句法分析结果
     * @param supposedParentLabel 从句父节点预期值
     * @return
     */
    public static List<SentencePattern> matchModificand(Tree clauseTree, CoreMap sentence, String supposedParentLabel) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        List<SentencePattern> sentencePatternList = new ArrayList<>();
        for (Tree match : tree.subTreeList()) {
            if (match.equals(clauseTree)) {
                // 获取从句起始位置，从0开始计数
                int clauseIndex = match.getLeaves().get(0).indexLeaves(0, false) - 2;
                // 获取从句（字符串）
                StringBuilder clause = new StringBuilder();
                for (Tree t : match.getLeaves()) {
                    clause.append(t.label().value()).append(" ");
                }
                // 获取其父节点
                String parentLabel = supposedParentLabel;
                if (supposedParentLabel == null) {
                    Tree parent = clauseTree.parent(tree);
                    parentLabel = parent.label().value();
                }
                TregexPattern leafPat = null;
                TregexPattern adverbLeafPat = null;
                // 根据父节点的类型获取从句所修饰的词的词性
                switch (parentLabel) {
                    case "NP":
                        leafPat = TregexPattern.compile("/^NN.*$/ == /^NN.*$/");
                        break;
                    case "VP":
                        leafPat = TregexPattern.compile("/^VB.*$/ == /^VB.*$/");
                        adverbLeafPat = TregexPattern.compile("/^RB.*$/ == /^RB.*$/");
                        break;
                    case "ADJP":
                        leafPat = TregexPattern.compile("/^JJ.*$/ == /^JJ.*$/");
                        adverbLeafPat = TregexPattern.compile("/^RB.*$/ == /^RB.*$/");
                        break;
                    case "ADVP":
                        leafPat = TregexPattern.compile("/^RB.*$/ == /^RB.*$/");
                        break;
                    default:
                        break;
                }
                if (leafPat != null) {
                    String lemma = null, adverbLemma = null, pos = null, adverbPos = null;
                    int modificandIndex = 0, adverbModificandIndex = 0;
                    // 匹配被修饰的词和其在句中的位置，从0开始计数
                    TregexMatcher leafMatcher = leafPat.matcher(tree);
                    while (leafMatcher.findNextMatchingNode()) {
                        int tempIndex = leafMatcher.getMatch().getLeaves().get(0).indexLeaves(0, false) - 2;
                        if (tempIndex >= clauseIndex) {
                            break;
                        }
                        modificandIndex = tempIndex;
                        CoreLabel coreLabel = sentence.get(CoreAnnotations.TokensAnnotation.class).get(modificandIndex);
                        lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);
                        pos = coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    }

                    if (adverbLeafPat != null) {
                        TregexMatcher adverbLeafMatcher = adverbLeafPat.matcher(tree);
                        while (adverbLeafMatcher.findNextMatchingNode()) {
                            int tempIndex = adverbLeafMatcher.getMatch().getLeaves().get(0).indexLeaves(0, false) - 2;
                            if (tempIndex >= clauseIndex) {
                                break;
                            }
                            adverbModificandIndex = tempIndex;
                            CoreLabel coreLabel = sentence.get(CoreAnnotations.TokensAnnotation.class).get(adverbModificandIndex);
                            adverbPos = coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                            adverbLemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);
                        }
                    }
                    sentencePatternList.add(new SentencePattern(null, lemma, pos, clause.toString(), null, null, null, null));
                    sentencePatternList.add(new SentencePattern(null, adverbLemma, adverbPos, clause.toString(), null, null, null, null));
                }
                break;
            } //if (match.equals(clauseTree))
        } // for (Tree match : tree.subTreeList())
        if (sentencePatternList.isEmpty()) {
            return null;
        }
        return sentencePatternList;
    }

    /**
     * 提取句子主干
     *
     * @param sentence 句法分析结果
     */
    public static List<String> getPrincipalClause(CoreMap sentence) {
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        List<String> resultList = new ArrayList<>();
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            String relation = edge.getRelation().toString();
            if (relation.startsWith("nsubj") && !relation.startsWith("nsubjpass")) {
                dealNsubj(edge, dependency, resultList);
            }
            else if (relation.startsWith("cop")) {
                dealCop(edge, dependency, resultList);
            }
            else if (relation.startsWith("xcomp")) {
                dealXcomp(edge, dependency, resultList);
            }
            else if (relation.startsWith("iobj")) {
                dealIobj(edge, dependency, resultList);
            }
            else if (relation.startsWith("nsubjpass")) {
                dealNsubjpass(edge, dependency, resultList);
            }
        }
        if (resultList.isEmpty()) {
            return null;
        }
        return resultList;
    }

    /**
     * 获取真正的主语、宾语等词（因为会有a tape of, a box of等修饰名词的情况）
     *
     * @param index  名词的位置
     * @param dependency
     * @return
     */
    public static Edge getRealNounEdge(int index, SemanticGraph dependency) {
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
     *  对句子中的从句进行抽象，方便句子主干提取
     *  大概策略：
     *      1、主语从句抽象为sb/sth
     *      2、宾语从句抽象为sb/sth
     *      3、表语从句抽象为sb/sth
     *      4、同位语从句、定语从句省略去除
     *      5、地点状语从句抽象为sp(some place)
     *      6、时间状语从句、原因状语从句、条件状语从句、结果状语从句都省略去除
     *
     * @param sentence
     */
    public static String abstractSentence (String sentence) {
        String sbOrSth = "somebodyOrSomeThing";
        String sp = "some place";
        StringBuilder resultSentence = new StringBuilder();
        List<CoreMap> coreMapList = StanfordParserUtil.parse(sentence);

        for (CoreMap coreMap : coreMapList) {
            StringBuilder tempSentenceBuilder = new StringBuilder();
            for (CoreLabel token : coreMap.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                tempSentenceBuilder.append(word).append(" ");
            }
            String tempSentence = tempSentenceBuilder.toString();
            // 识别从句类型（还需考虑从句中包含从句的情况），根据从句类型对句子进行抽象
            List<SentencePattern> sentencePatternList = findAllSpecialSentencePattern(coreMap);
            for (SentencePattern sentencePattern : sentencePatternList) {
                int type = sentencePattern.getType();
                String clauseContent = sentencePattern.getClauseContent();
                switch (type) {
                    // 主语从句、宾语从句、表语从句均抽象为sb/sth
                    case 1:
                    case 2:
                    case 4:
                        tempSentence = tempSentence.replace(clauseContent.trim().toLowerCase(), sbOrSth);
                        break;

                    // 定语从句或同位语从句 则省略
                    case 3:
                        tempSentence = tempSentence.replaceAll(clauseContent.trim().toLowerCase(), "");
                        break;

                    // 状语从句
                    case 5:
                        // 判断从句类型
                        String firstWordOfClause = clauseContent.split(" ")[0];
                        // 为地点状语从句
                        if (CorpusConstant.PLACE_ADVERBIAL_CLAUSE_CONJECTION_SET.contains(firstWordOfClause)) {
                            tempSentence = tempSentence.replaceAll(clauseContent.trim().toLowerCase(), sp);
                        }
                        // 为其他状语从句
                        else {
                            tempSentence = tempSentence.replaceAll(clauseContent.trim(), "");
                        }
                        break;

                    default:
                        break;
                }
            }// for (SentencePattern sentencePattern : sentencePatternList)
            resultSentence.append(tempSentence).append(" ");
        }// for (CoreMap coreMap : coreMapList)

        return resultSentence.toString();
    }

    /**
     * 集合转列表
     *
     * @param set
     * @return
     */
    private static List<SentencePattern> setToList (Set<String> set) {
        if (set.isEmpty()) {
            return null;
        }
        else {
            List<SentencePattern> sentencePatternList = new ArrayList<>();
            for (String clause : set) {
                sentencePatternList.add(
                        new SentencePattern(Integer.valueOf(clause.split("_")[0]), null, null, clause.split("_")[1], null, null, null, null)
                );
            }
            return sentencePatternList;
        }
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
        StringBuilder complement = new StringBuilder().append(startComplement);
        // 循环查找，看是否还有补语
        boolean hasComplement = true;
        while (hasComplement) {
            hasComplement = false;
            for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                if (semanticGraphEdge.getRelation().toString().equals("xcomp") &&
                    semanticGraphEdge.getGovernor().index() == startIndex) {
                    complement.append(" ").append(semanticGraphEdge.getDependent().word());
                    startIndex = semanticGraphEdge.getDependent().index();
                    hasComplement = true;
                } else if (semanticGraphEdge.getRelation().toString().equals("dobj") &&
                           semanticGraphEdge.getGovernor().index() == startIndex){
                    complement.append(" ").append(semanticGraphEdge.getDependent().word());
                    hasComplement = false;
                } else if (CorpusConstant.COLLOCATION_NOMD_RELATION_SET.contains(semanticGraphEdge.getRelation().toString()) &&
                           semanticGraphEdge.getGovernor().index() == startIndex) {
                    String prep = semanticGraphEdge.getRelation().toString().split(":")[1];
                    complement.append(" ").append(prep).append(" ").append(semanticGraphEdge.getDependent().word());
                    hasComplement = false;
                }
            }
        } // while (hasComplement)

        return complement.toString();
    }

    /**************************************************** 一下部分为句子主干提取相关 *****************************************************/

    /**
     * 提取句子主干中，对“nsubj”的关系进行处理。因方法体较长，所以单独抽取出来。
     *
     * @param edge
     * @param dependency
     * @param resultList
     */
    private static void dealNsubj(SemanticGraphEdge edge, SemanticGraph dependency, List<String> resultList) {
        String govAndDep = edge.getGovernor().tag() + "-" + edge.getDependent().tag();
        // 匹配是否包含主谓宾结构
        if (govAndDep.matches("(VB[A-Z]{0,1})-(NN[A-Z]{0,1})") ||
            govAndDep.matches("(VB[A-Z]{0,1})-PRP")) {
            Edge temp = getRealNounEdge(edge.getDependent().index(), dependency);
            String subject = (temp == null ? edge.getDependent().word() : temp.getWord());
            String predicate = edge.getGovernor().word();
            String object = "";
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
                            temp = getRealNounEdge(semanticGraphEdge.getDependent().index(), dependency);
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
                        }
                    } else if (objGovAndDep.matches("(VB[A-Z]{0,1})-(JJ[A-Z]{0,1})")) {
                        // 通过单词位置判断是否为同一个谓语
                        if (semanticGraphEdge.getGovernor().index() == predicateIndex) {
                            int objectIndex = semanticGraphEdge.getDependent().index();
                            // 找到形容词短语
                            for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                                if (se.getRelation().toString().startsWith("nmod:") &&
                                    se.getGovernor().index() == objectIndex) {
                                    object = se.getGovernor().word() + " " + se.getRelation().toString().split(":")[1] + " " + se.getDependent().lemma();
                                }
                            }
                            hasObject = true;
                        }
                    }
                    if (hasObject && !isDoubleObject) {
                        System.out.println("主谓宾：" + subject + " " + predicate + " " + object);
                        resultList.add(subject + " " + predicate + " " + object);
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
                        complement = getComplement(startIndex, complement, dependency);
                        System.out.println("主谓+短语：" + subject + " " + predicate + " " + complement);
                        resultList.add(subject + " " + predicate + " " + complement);
                        break;
                    }
                }

                // 若没有补语，则可能为短语搭配 nmod:介词
                for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                    if (CorpusConstant.COLLOCATION_NOMD_RELATION_SET.contains(se.getRelation().toString()) &&
                        se.getGovernor().index() == predicateIndex) {
                        String prep = se.getRelation().toString().split(":")[1];
                        complement = prep + " " + se.getDependent().word();
                        System.out.println("主谓+短语：" + subject + " " + predicate + " " + complement);
                        resultList.add(subject + " " + predicate + " " + complement);
                    }
                }

            } // if 主谓 + 补语
        }
    }

    /**
     * 提取句子主干中，对“cop”的关系进行处理。。
     *
     * @param edge
     * @param dependency
     * @param resultList
     */
    private static void dealCop (SemanticGraphEdge edge, SemanticGraph dependency, List<String> resultList) {
        // 匹配是否包含主系表结构
        String copula = edge.getDependent().word();
        Edge temp = getRealNounEdge(edge.getGovernor().index(), dependency);
        String predicative = (temp == null ? edge.getGovernor().word() : temp.getWord());
        int predicativeIndex = edge.getGovernor().index();
        // 寻找nsubj依存关系，找出系动词的主语
        for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
            if (semanticGraphEdge.getRelation().toString().equals("nsubj")) {
                // 通过单词位置判断是否是同一个表语
                if (semanticGraphEdge.getGovernor().index() == predicativeIndex) {
                    temp =  getRealNounEdge(semanticGraphEdge.getDependent().index(), dependency);
                    String subject = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());
                    System.out.println("主系表：" + subject + " " + copula + " " + predicative);
                    resultList.add(subject + " " + copula + " " + predicative);
                }
            }
        }
    }

    /**
     * 提取句子主干中，对“iobj”的关系进行处理。。
     *
     * @param edge
     * @param dependency
     * @param resultList
     */
    private static void dealIobj (SemanticGraphEdge edge, SemanticGraph dependency, List<String> resultList) {
        // 匹配是否为双宾语句
        String subject;
        String verb = edge.getGovernor().word();
        int verbIndex = edge.getGovernor().index();
        Edge temp = getRealNounEdge(edge.getDependent().index(), dependency);
        String indirectObject = (temp == null ? edge.getDependent().word() : temp.getWord());
        String directObject;
        // 寻找nsubj依存关系，找出主语
        for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
            if (semanticGraphEdge.getRelation().toString().equals("nsubj") &&
                semanticGraphEdge.getGovernor().index() == verbIndex) {
                temp = getRealNounEdge(semanticGraphEdge.getDependent().index(), dependency);
                subject = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());
                // 寻找dobj关系，找出直接宾语
                for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                    if (se.getRelation().toString().equals("dobj") &&
                            se.getGovernor().index() == verbIndex) {
                        temp = getRealNounEdge(se.getDependent().index(), dependency);
                        directObject = (temp == null ? se.getDependent().word() : temp.getWord());
                        System.out.println("双宾语" + subject + " " + verb + " " + indirectObject + " " + directObject);
                        resultList.add(subject + " " + verb + " " + indirectObject + " " + directObject);
                    }
                }
            }
        }
    }

    /**
     * 提取句子主干中，对“xcomp”的关系进行处理。因方法体较长，所以单独抽取出来。
     *
     * @param edge
     * @param dependency
     * @param resultList
     */
    private static void dealXcomp(SemanticGraphEdge edge, SemanticGraph dependency, List<String> resultList) {
        String govAndDep = edge.getGovernor().tag() + "-" + edge.getDependent().tag();
        // 匹配是否为双宾语结构
        if (govAndDep.matches("(VB[A-Z]{0,1})-(NN[A-Z]{0,1})") &&
                CorpusConstant.DOUBLE_OBJECT_VERB_SET.contains(edge.getGovernor().lemma().toLowerCase())){
            String verb = edge.getGovernor().word();
            int verbIndex = edge.getGovernor().index();
            int directObjectIndex = edge.getDependent().index();
            Edge temp = getRealNounEdge(directObjectIndex, dependency);
            String directObject = (temp == null ? edge.getDependent().word() : temp.getWord());
            // 寻找nsubj依存关系，找出主语
            for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                if (semanticGraphEdge.getRelation().toString().equals("nsubj") &&
                        semanticGraphEdge.getGovernor().index() == verbIndex) {
                    temp = getRealNounEdge(semanticGraphEdge.getDependent().index(), dependency);
                    String subject = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());
                    // 寻找nsubj依存关系，找出间接宾语
                    for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                        if (se.getRelation().toString().equals("nsubj") &&
                                se.getGovernor().index() == directObjectIndex) {
                            temp = getRealNounEdge(se.getDependent().index(), dependency);
                            String inderectObject = (temp == null ? se.getDependent().word() : temp.getWord());
                            System.out.println("双宾语：" + subject + " " + verb + " " + inderectObject + " " + directObject);
                            resultList.add(subject + " " + verb + " " + inderectObject + " " + directObject);
                        }
                    }
                }
            }
        }

        // 匹配是否为其他主系表结构，系动词主要是一些become，get，look等动词
        else if (edge.getGovernor().tag().matches("VB[A-Z]{0,1}") &&
                CorpusConstant.COPULA_LEMMA_SET.contains(edge.getGovernor().lemma().toLowerCase())) {

            String copula = edge.getGovernor().word();
            int copulaIndex = edge.getGovernor().index();
            Edge temp = getRealNounEdge(edge.getDependent().index(), dependency);
            String predicative = (temp == null ? edge.getDependent().word() : temp.getWord());
            int predicativeIndex =  (temp == null ? edge.getDependent().index() : temp.getIndex());
            String complement = "";
            int startIndex = predicativeIndex;
            complement = getComplement(startIndex, "", dependency);
            // 寻找nsubj依存关系，找出系动词的主语
            for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                // 通过单词位置判断是否是同一个系动词
                if (semanticGraphEdge.getRelation().toString().equals("nsubj") &&
                        semanticGraphEdge.getGovernor().index() == copulaIndex) {
                    temp = getRealNounEdge(semanticGraphEdge.getDependent().index(), dependency);
                    String subject = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());
                    System.out.println("主系表：" + subject + " " + copula + " " + predicative + " " + complement);
                    resultList.add(subject + " " + copula + " " + predicative + " " + complement);
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
                    Edge temp = getRealNounEdge(semanticGraphEdge.getDependent().index(), dependency);
                    String subject = (temp == null ? semanticGraphEdge.getDependent().word() : temp.getWord());
                    // 寻找nsubj依存关系，找出宾语
                    for (SemanticGraphEdge se : dependency.edgeListSorted()) {
                        if (se.getRelation().toString().equals("nsubj") &&
                                se.getGovernor().index() == complementIndex) {
                            temp = getRealNounEdge(se.getDependent().index(), dependency);
                            String object = (temp == null ? se.getDependent().word() : temp.getWord());
                            System.out.println("主谓宾+宾补：" + subject + " " + verb + " " + object + " " + complement);
                            resultList.add(subject + " " + verb + " " + object + " " + complement);
                        }
                    }
                }
            }
        }
    }

    /**
     * 提取句子主干中，对“nsubjpass”的关系进行处理。因方法体较长，所以单独抽取出来。
     *
     * @param edge
     * @param dependency
     * @param resultList
     */
    private static void dealNsubjpass(SemanticGraphEdge edge, SemanticGraph dependency, List<String> resultList) {
        // 识别被动语态
        Edge temp = getRealNounEdge(edge.getDependent().index(), dependency);
        String subject = (temp == null ? edge.getDependent().word() : temp.getWord());
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
                        Edge tempEdge = getRealNounEdge(se.getDependent().index(), dependency);
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
                                resultList.add(subject + " " + be + " " +passiveVerb + " " + prep + " " + agent);
                            }
                        }
                    }
                }

                if (!hasAgent) {
                    System.out.println("被动：" + subject + " " + be + " " +passiveVerb);
                    resultList.add(subject + " " + be + " " +passiveVerb);
                }
            }
        }
    }

    public static void main(String[] args) {
        String text = " \t\t\tMy friends dislike me because I’m handsome and successful.\n";
        String shorterText = abstractSentence(text);
        System.out.println("抽象后的句子：" + shorterText);
        System.out.println(getPrincipalClause(StanfordParserUtil.parse(shorterText.toLowerCase()).get(0)));

        List<CoreMap> result = StanfordParserUtil.parse(text.toLowerCase());
        for(CoreMap sentence : result) {
            System.out.println(hasSoThat(sentence));
            System.out.println(hasInvertedStructure(sentence));
            System.out.println(getEmphaticStructure(sentence));
            sentence.get(TreeCoreAnnotations.TreeAnnotation.class).pennPrint();
            for (SentencePattern sp : findAllSpecialSentencePattern(sentence)) {
                System.out.println(sp.toString());
            }
        }
    }
}
