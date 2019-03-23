package com.hugailei.graduation.corpus.util;

import com.bfsuolframework.core.utils.StringUtils;
import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dao.RankWordDao;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * @author HU Gailei
 * @date 2019/3/15
 * <p>
 * description: 句子等级获取相关操作
 * </p>
 **/
@Slf4j
@Component
public class SentenceRankUtil {
    private static String MODEL_PATH = CorpusConstant.SVM_MODEL_PATH;

    private static svm_model SVM_MODEL;

    @PostConstruct
    public void init(){
        try {
            SVM_MODEL = svm.svm_load_model(MODEL_PATH);
        } catch (IOException e) {
            log.error("static block | error: {}", e);
        }
    }

    /**
     * 计算，得到句子各个特征值
     *
     * @param sentence
     * @return
     */
    private static String featureValue (String sentence) {
        // 读取数据库，存储各级词汇
        Set<String> rank1WordSet = CorpusConstant.RANK_NUM_TO_WORD_SET.get(1);
        Set<String> rank2WordSet = CorpusConstant.RANK_NUM_TO_WORD_SET.get(2);
        Set<String> rank3WordSet = CorpusConstant.RANK_NUM_TO_WORD_SET.get(3);
        Set<String> rank4WordSet = CorpusConstant.RANK_NUM_TO_WORD_SET.get(4);
        Set<String> rank5WordSet = CorpusConstant.RANK_NUM_TO_WORD_SET.get(5);
        Set<String> rank6WordSet = CorpusConstant.RANK_NUM_TO_WORD_SET.get(6);
        List<CoreMap> result = StanfordParserUtil.parse(sentence);
        String featureValueString = null;
        for (CoreMap coreMap : result) {
            // 计算句子长度
            int clauseCount = 0;
            // 计算平均词长及各级单词数
            int totalWordLength = 0;
            int wordCount = 0;
            int level1WordCount = 0, level2WordCount = 0, level3WordCount = 0;
            int level4WordCount = 0, level5WordCount = 0, level6WordCount = 0;

            // 专有名词数量
            int properNounCount = 0;
            // 名词数量，动词数量，代词数量
            int nounCount = 0, verbCount = 0, pronounCount = 0, adjCount = 0, advCount = 0, prepCount = 0;
            Set<String> wordSet = new HashSet<>();
            for (CoreLabel token : coreMap.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                wordCount++;
                totalWordLength += word.length();

                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                boolean found = false;
                if (rank1WordSet.contains(lemma)) {
                    level1WordCount++;
                    found = true;
                }
                if (!found) {
                    if (rank2WordSet.contains(lemma)) {
                        level2WordCount++;
                        found = true;
                    }
                }
                if (!found) {
                    if (rank3WordSet.contains(lemma)) {
                        level3WordCount++;
                        found = true;
                    }
                }
                if (!found) {
                    if (rank4WordSet.contains(lemma)) {
                        level4WordCount++;
                        found = true;
                    }
                }
                if (!found) {
                    if (rank5WordSet.contains(lemma)) {
                        level5WordCount++;
                        found = true;
                    }
                }
                if (!found) {
                    if (rank6WordSet.contains(lemma)) {
                        level6WordCount++;
                    }
                }

                // 计算专有名词数量
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (!ner.equals("O")) {
                    properNounCount++;
                }

                // 计算各词性词汇数量
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (pos.matches("NN.*")) {
                    nounCount++;
                } else if (pos.matches("VB.*")) {
                    verbCount++;
                } else if (pos.matches("RB.*")) {
                    advCount++;
                } else if (pos.matches("JJ.*")) {
                    adjCount++;
                } else if (pos.equals("PRP")) {
                    pronounCount++;
                } else if (pos.equals("IN") || pos.equals("RP")) {
                    prepCount++;
                }
                wordSet.add(word.toLowerCase());
            } //  for (CoreLabel token )

            // 计算从句个数及最大从句长度
            int maxClauseLength = 0;
            Tree tree = coreMap.get(TreeCoreAnnotations.TreeAnnotation.class);
            TregexMatcher matcher = TregexPattern.compile("SBAR | S").matcher(tree);
            while (matcher.findNextMatchingNode()) {
                clauseCount++;
                int clauseLength = matcher.getMatch().getLeaves().size();
                if (clauseLength >= maxClauseLength) {
                    maxClauseLength = clauseLength;
                }
            }

            // 计算句法树深度
            int treeDepth = tree.depth();

            // 计算各短语的数量
            int nounPhraseCount = 0, verbPhraseCount = 0, adjPhraseCount = 0, advPhraseCount = 0, prepPhraseCount = 0;
            matcher = TregexPattern.compile("NP").matcher(tree);
            while (matcher.findNextMatchingNode()) {
                nounPhraseCount++;
            }
            matcher = TregexPattern.compile("PP").matcher(tree);
            while (matcher.findNextMatchingNode()) {
                prepPhraseCount++;
            }
            matcher = TregexPattern.compile("VP").matcher(tree);
            while (matcher.findNextMatchingNode()) {
                verbPhraseCount++;
            }
            matcher = TregexPattern.compile("ADJP").matcher(tree);
            while (matcher.findNextMatchingNode()) {
                adjPhraseCount++;
            }
            matcher = TregexPattern.compile("ADVP").matcher(tree);
            while (matcher.findNextMatchingNode()) {
                advPhraseCount++;
            }

            // 计算类符/形符比
            int typeCount = wordSet.size();
            double ratio = Double.valueOf(String.valueOf(typeCount)) / Double.valueOf(String.valueOf(wordCount));

            // 计算平均词长
            int avgWordLength = totalWordLength / wordCount;
            featureValueString = "1:" + level1WordCount + " " +
                    "2:" + level2WordCount + " " +
                    "3:" + level3WordCount + " " +
                    "4:" + level4WordCount + " " +
                    "5:" + level5WordCount + " " +
                    "6:" + level6WordCount + " " +
                    "7:" + wordCount + " " +
                    "8:" + ratio + " " +
                    "9:" + avgWordLength + " " +
                    "10:" + clauseCount + " " +
                    "11:" + properNounCount + " " +
                    "12:" + nounCount + " " +
                    "13:" + verbCount + " " +
                    "14:" + adjCount + " " +
                    "15:" + advCount + " " +
                    "16:" + pronounCount + " " +
                    "17:" + prepCount + " " +
                    "18:" + nounPhraseCount + " " +
                    "19:" + verbPhraseCount + " " +
                    "20:" + adjPhraseCount + " " +
                    "21:" + advPhraseCount + " " +
                    "22:" + prepPhraseCount + " " +
                    "23:" + treeDepth + " " +
                    "24:" + maxClauseLength;
            break;
        }
        return featureValueString;
    }

    /**
     * 获取句子的难度等级
     *
     * @param sentence
     * @return
     */
    public static int sentenceRankNum (String sentence) {

        if (StringUtils.isBlank(sentence)) {
            return 0;
        }

        String featureValueString = featureValue(sentence);
        StringTokenizer st = new StringTokenizer(featureValueString," \t\n\r\f:");
        int m = st.countTokens()/2;
        svm_node[] x = new svm_node[m];
        for(int j=0; j<m ;j++)
        {
            x[j] = new svm_node();
            x[j].index = Integer.parseInt(st.nextToken());
            x[j].value = Double.valueOf(st.nextToken()).doubleValue();
        }
        double v = svm.svm_predict(SVM_MODEL, x);
        return new Double(v).intValue();
    }

    /**
     * 对句子列表的所有句子按照难度等级升序排序
     *
     * @param sentenceList
     * @return
     */
    public static List<String> orderSentenceByRankNumAsc(List<String> sentenceList) {
        log.info("orderSentenceByRankNumAsc | start to rank.");
        Map<String, Integer> sentence2RankNum = new TreeMap<String, Integer>();
        for (String sentence: sentenceList) {
            int rankNum = sentenceRankNum(sentence);
            sentence2RankNum.put(sentence, rankNum);
        }

        //这里将map.entrySet()转换成list
        List<Map.Entry<String, Integer>> mapEntryList = new ArrayList<Map.Entry<String, Integer>>(
                sentence2RankNum.entrySet()
        );
        //然后通过比较器来实现排序
        Collections.sort(mapEntryList, new Comparator<Map.Entry<String, Integer>>() {
            //升序排序
            @Override
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        List<String> resultList = new ArrayList<>();
        for (Map.Entry entry : mapEntryList) {
            String s = (String)entry.getKey();
            resultList.add(s);
        }
        return resultList;
    }
}
