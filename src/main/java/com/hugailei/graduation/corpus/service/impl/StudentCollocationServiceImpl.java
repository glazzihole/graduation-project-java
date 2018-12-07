package com.hugailei.graduation.corpus.service.impl;

import com.bfsuolframework.core.utils.StringUtils;
import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.service.StudentCollocationService;
import com.hugailei.graduation.corpus.util.SentencePatternUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.hugailei.graduation.corpus.service.impl.CollocationServiceImpl.sortCollocationDtoList;

/**
 * @author HU Gailei
 * @date 2018/11/26
 * <p>
 * description:
 * </p>
 **/
@Service
@Slf4j
public class StudentCollocationServiceImpl implements StudentCollocationService {

    private final static String NOT_IMPORTANT = "notsoimportant";

    @Override
    public CollocationDto.CollocationInfo getCollocationInText(String text) {
        try {
            log.info("getCollocationInText | text: {}", text);
            Map<String, Integer> lemmaCollocationKey2Freq = new HashMap<>();
            Map<String, Integer> posCollocationKey2Freq = new HashMap<>();
            List<CoreMap> sentences = StanfordParserUtil.parse(text);
            for (CoreMap sentence : sentences) {
                SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
                for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
                    String relation = edge.getRelation().toString();
                    int govIndex = edge.getGovernor().index();
                    int depIndex = edge.getDependent().index();
                    boolean found = false;
                    String firstWord = null, secondWord = null, firstPos = null, secondPos = null, thirdWord = null, thirdPos = null;
                    if (CorpusConstant.COLLOCATION_DEPENDENCY_RELATION_SET.contains(relation)) {
                        if ((relation.startsWith("nsubj") && !relation.startsWith("nsubjpass")) ||
                            "nmod:agent".equals(relation)) {
                            String adjNounRegex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                            String nounverbRegex = "((NN[A-Z]{0,1})|(PRP))-(VB[A-Z]{0,1})";
                            SentencePatternUtil.Edge temp = SentencePatternUtil.getRealNounEdge(edge.getDependent().index(), dependency);
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(adjNounRegex)) {
                                firstWord = edge.getGovernor().lemma();
                                secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                firstPos = edge.getGovernor().tag();
                                secondPos = edge.getDependent().tag();
                                found = true;
                            } else if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(nounverbRegex)) {
                                firstWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                secondWord = edge.getGovernor().lemma();
                                firstPos = edge.getDependent().tag();
                                secondPos = edge.getGovernor().tag();
                                found = true;
                            }
                        }
                        else if (relation.startsWith("dobj") || relation.startsWith("nsubjpass")) {
                            String verbNounRegex = "(VB[A-Z]{0,1})-((NN[A-Z]{0,1})|(PRP))";
                            SentencePatternUtil.Edge temp = SentencePatternUtil.getRealNounEdge(edge.getDependent().index(), dependency);
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbNounRegex)) {
                                firstWord = edge.getGovernor().lemma();
                                firstPos = edge.getGovernor().tag();
                                secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                secondPos = edge.getDependent().tag();
                                found = true;
                            }
                        }
                        else if (relation.startsWith("csubj")) {
                            String verbNounRegex = "(VB[A-Z]{0,1})-((NN[A-Z]{0,1})|(PRP))";
                            SentencePatternUtil.Edge temp = SentencePatternUtil.getRealNounEdge(edge.getGovernor().index(), dependency);
                            if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(verbNounRegex)) {
                                firstWord = edge.getDependent().lemma();
                                secondWord = (temp == null ? edge.getGovernor().lemma() : temp.getLemma());
                                firstPos = edge.getDependent().tag();
                                secondPos = edge.getGovernor().tag();
                                found = true;
                            }
                        }
                        else if (relation.startsWith("amod")) {
                            String adjNounRegex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                            SentencePatternUtil.Edge temp = SentencePatternUtil.getRealNounEdge(edge.getGovernor().index(), dependency);
                            if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(adjNounRegex)) {
                                firstWord = edge.getDependent().lemma();
                                firstPos = edge.getDependent().tag();
                                secondWord = (temp == null ? edge.getGovernor().lemma() : temp.getLemma());
                                secondPos = edge.getGovernor().tag();
                                found = true;
                            }
                        }
                        else if (relation.startsWith("advmod")) {
                            String verbAdvRegex = "(VB[A-Z]{0,1})-(RB[A-Z]{0,1})";
                            String adjAdvRegex = "(JJ[A-Z]{0,1})-(RB[A-Z]{0,1})";
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbAdvRegex) ||
                                    (edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(adjAdvRegex)) {
                                // 根据单词在句子中的位置调整在搭配中的先后顺序
                                if (govIndex < depIndex) {
                                    firstWord = edge.getGovernor().lemma();
                                    firstPos = edge.getGovernor().tag();
                                    secondWord = edge.getDependent().lemma();
                                    secondPos = edge.getDependent().tag();
                                } else {
                                    firstWord = edge.getDependent().lemma();
                                    secondWord = edge.getGovernor().lemma();
                                    firstPos = edge.getDependent().tag();
                                    secondPos = edge.getGovernor().tag();
                                }
                                found = true;
                            }
                        }
                        else if ("compound:prt".equals(relation) || "nmod".equals(relation)) {
                            firstWord = edge.getGovernor().lemma();
                            firstPos = edge.getGovernor().tag();
                            secondWord = edge.getDependent().lemma();
                            secondPos = edge.getDependent().tag();
                            found = true;
                        }
                        else if (relation.startsWith("xcomp")) {
                            String verbAdjRegex = "(VB[A-Z]{0,1})-(JJ[A-Z]{0,1})";
                            String verbNounRegex = "(VB[A-Z]{0,1})-((NN[A-Z]{0,1})|(PRP))";
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbAdjRegex) ||
                                    (edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbNounRegex)) {
                                SentencePatternUtil.Edge temp = null;
                                if (edge.getDependent().tag().startsWith("NN")) {
                                    temp = SentencePatternUtil.getRealNounEdge(edge.getDependent().index(), dependency);
                                }
                                firstWord = edge.getGovernor().lemma();
                                secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                secondPos = edge.getDependent().tag();
                                firstPos = edge.getGovernor().tag();
                                found = true;
                            }

                            // 当第二个词为形容词是，判断动词是否为系统词，若是，则后面的形容词也可以修饰该动词的主语
                            if (edge.getDependent().tag().matches("JJ[A-Z]{0,1}")) {
                                if (CorpusConstant.COPULA_LEMMA_SET.contains(edge.getGovernor().lemma())) {
                                    int verbIndex = edge.getGovernor().index();
                                    for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                                        if (semanticGraphEdge.getRelation().toString().startsWith("nsubj") &&
                                            !semanticGraphEdge.getRelation().toString().startsWith("nsubjpass") &&
                                            semanticGraphEdge.getGovernor().index() == verbIndex) {
                                            int subjectIndex = semanticGraphEdge.getDependent().index();
                                            SentencePatternUtil.Edge subjectTemp = SentencePatternUtil.getRealNounEdge(subjectIndex, dependency);
                                            String subject = (subjectTemp == null ? semanticGraphEdge.getDependent().lemma() : subjectTemp.getLemma());

                                            // 存储到Map中
                                            String lemmaCollocationKey = (edge.getDependent().lemma() + "_JJ_" + subject + "_NN").toLowerCase();
                                            int freq = 1;
                                            if (lemmaCollocationKey2Freq.containsKey(lemmaCollocationKey)) {
                                                freq = lemmaCollocationKey2Freq.get(lemmaCollocationKey) + 1;
                                            }
                                            lemmaCollocationKey2Freq.put(lemmaCollocationKey, freq);

                                            String posCollocationKey = (edge.getDependent().lemma() + "_JJ_" + NOT_IMPORTANT + "_NN").toLowerCase();
                                            freq = 1;
                                            if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                                                freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
                                            }
                                            posCollocationKey2Freq.put(posCollocationKey, freq);
                                        }
                                    }
                                }
                            }
                        }
                        else if ("dep".equals(relation)) {
                            if (edge.getGovernor().tag().matches("VB[A-Z]{0,1}")) {
                                found = true;
                                firstWord = edge.getGovernor().lemma();
                                firstPos = edge.getGovernor().tag();
                                secondWord = edge.getDependent().lemma();
                                secondPos = edge.getDependent().tag();
                                if (edge.getDependent().tag().startsWith("NN")) {
                                    SentencePatternUtil.Edge temp = SentencePatternUtil.getRealNounEdge(edge.getDependent().index(), dependency);
                                    secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                }
                            }
                        }
                    } else if (CorpusConstant.COLLOCATION_NOMD_RELATION_SET.contains(relation)) {
                        firstWord = edge.getGovernor().lemma();
                        firstPos = edge.getGovernor().tag();
                        secondWord = relation.split(":")[1];
                        secondPos = "IN";
                        thirdWord = edge.getDependent().lemma();
                        thirdPos = edge.getDependent().tag();
                        if (thirdPos.startsWith("NN")) {
                            SentencePatternUtil.Edge temp = SentencePatternUtil.getRealNounEdge(edge.getDependent().index(), dependency);
                            if (temp != null) {
                                thirdWord = temp.getLemma();
                            }
                        }
                        found = true;
                    }

                    if (found) {
                        // 词性同一存储为该词性下原型的词性
                        firstPos = StanfordParserUtil.getBasePos(firstPos);
                        secondPos = StanfordParserUtil.getBasePos(secondPos);

                        if (!StringUtils.isBlank(thirdWord)) {
                            thirdPos = StanfordParserUtil.getBasePos(thirdPos);
                        }

                        // 原型搭配情况统计
                        fillLemmaCollocationKey2Freq(firstWord, firstPos, secondWord, secondPos, thirdWord, thirdPos, lemmaCollocationKey2Freq);
                        // 词性搭配情况统计
                        fillPosCollocationKey2Freq(firstWord, firstPos, secondWord, secondPos, thirdWord, thirdPos, posCollocationKey2Freq);
                    }

                } // for (SemanticGraphEdge edge : dependency.edgeListSorted())
            } // for (CoreMap sentence : sentences)

            // 遍历map，获取搭配频次，填充返回结果
            List<CollocationDto> lemmaCollocationList = getLemmaCollocationList(lemmaCollocationKey2Freq);
            sortCollocationDtoList(lemmaCollocationList);

            List<CollocationDto> posCollocationList = getPosCollocationList(posCollocationKey2Freq);
            sortCollocationDtoList(posCollocationList);

            CollocationDto.CollocationInfo collocationInfo = new CollocationDto.CollocationInfo();
            collocationInfo.setWordCollocationList(lemmaCollocationList);
            collocationInfo.setPosCollocationList(posCollocationList);
            return collocationInfo;
        } catch (Exception e) {
            log.error("getCollocationInText | error: {}", e);
            return null;
        }
    }

    /**
     * 原型搭配情况统计
     *
     * @param firstWord
     * @param firstPos
     * @param secondWord
     * @param secondPos
     * @param thirdWord
     * @param thirdPos
     * @param key2FreqMap
     */
    private void fillLemmaCollocationKey2Freq (String firstWord,
                                               String firstPos,
                                               String secondWord,
                                               String secondPos,
                                               String thirdWord,
                                               String thirdPos,
                                               Map<String, Integer> key2FreqMap) {
        String lemmaCollocationKey = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();
        int freq = 1;
        if (key2FreqMap.containsKey(lemmaCollocationKey)) {
            freq = key2FreqMap.get(lemmaCollocationKey) + 1;
        }
        key2FreqMap.put(lemmaCollocationKey, freq);

        if (!StringUtils.isBlank(thirdWord)) {
            freq = 1;
            lemmaCollocationKey = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos + "_" + thirdWord + "_" + thirdPos).toLowerCase();
            if (key2FreqMap.containsKey(lemmaCollocationKey)) {
                freq = key2FreqMap.get(lemmaCollocationKey) + 1;
            }
            key2FreqMap.put(lemmaCollocationKey, freq);
        }
    }

    /**
     * 词性搭配情况统计，在一些搭配中，忽略不重要的信息，只关注词性。
     * 如动名词的搭配中，可以忽略名词具体是哪个单词，只关注这个动词和名词词性搭配了多少次
     *
     * @param firstWord
     * @param firstPos
     * @param secondWord
     * @param secondPos
     * @param thirdWord
     * @param thirdPos
     * @param key2FreqMap
     */
    private void fillPosCollocationKey2Freq (String firstWord,
                                             String firstPos,
                                             String secondWord,
                                             String secondPos,
                                             String thirdWord,
                                             String thirdPos,
                                             Map<String, Integer> key2FreqMap) {
        String posCollocationKey;
        int freq = 1;

        // 将所有的代词词性视为名词词性
        String posPair = firstPos + "-" + secondPos.replace("PRP", "NN");
        switch (posPair){
            case "NN-VB":
                posCollocationKey = (NOT_IMPORTANT + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();
                if (key2FreqMap.containsKey(posCollocationKey)) {
                    freq = key2FreqMap.get(posCollocationKey) + 1;
                }
                key2FreqMap.put(posCollocationKey, freq);
                break;

            case "VB-NN":
            case "JJ-NN":
            case "VB-IN":
            case "VB-RP":
            case "NN-IN":
            case "JJ-IN":
                posCollocationKey = (firstWord + "_" + firstPos + "_" + NOT_IMPORTANT + "_" + secondPos).toLowerCase();
                if (key2FreqMap.containsKey(posCollocationKey)) {
                    freq = key2FreqMap.get(posCollocationKey) + 1;
                }
                key2FreqMap.put(posCollocationKey, freq);
                break;

            case "VB-RB":
            case "RB-VB":
            case "JJ-RB":
            case "RB-JJ":
            case "VB-JJ":
                posCollocationKey = (firstWord + "_" + firstPos + "_" + NOT_IMPORTANT + "_" + secondPos).toLowerCase();
                if (key2FreqMap.containsKey(posCollocationKey)) {
                    freq = key2FreqMap.get(posCollocationKey) + 1;
                }
                key2FreqMap.put(posCollocationKey, freq);

                posCollocationKey = (NOT_IMPORTANT + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();
                if (key2FreqMap.containsKey(posCollocationKey)) {
                    freq = key2FreqMap.get(posCollocationKey) + 1;
                }
                key2FreqMap.put(posCollocationKey, freq);
                break;

            default:
                break;
        }// switch

        if (!StringUtils.isBlank(thirdWord)) {
            freq = 1;
            posCollocationKey = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos + "_" + NOT_IMPORTANT + "_" + thirdPos).toLowerCase();
            if (key2FreqMap.containsKey(posCollocationKey)) {
                freq = key2FreqMap.get(posCollocationKey) + 1;
            }
            key2FreqMap.put(posCollocationKey, freq);
        }
    }

    /**
     * 获取原型搭配列表
     *
     * @param key2Freq
     * @return
     */
    private List<CollocationDto> getLemmaCollocationList(Map<String, Integer> key2Freq) {
        List<CollocationDto> lemmaCollocationList = new ArrayList<>();
        for (Map.Entry entry : key2Freq.entrySet()) {
            CollocationDto collocationDto = new CollocationDto();
            String key = (String)entry.getKey();
            int freq = (Integer) entry.getValue();
            collocationDto.setFirstWord(key.split("_")[0]);
            collocationDto.setFirstPos(key.split("_")[1].toUpperCase());
            collocationDto.setSecondWord(key.split("_")[2]);
            collocationDto.setSecondPos(key.split("_")[3].toUpperCase());
            collocationDto.setFreq(freq);
            if (key.split("_").length == 4) {
                lemmaCollocationList.add(collocationDto);
            } else if (key.split("_").length == 6) {
                collocationDto.setThirdWord(key.split("_")[4]);
                collocationDto.setThirdPos(key.split("_")[5].toUpperCase());
                lemmaCollocationList.add(collocationDto);
            }
        }

        return lemmaCollocationList;
    }

    /**
     * 获取词性搭配列表
     *
     * @param key2Freq
     * @return
     */
    private List<CollocationDto> getPosCollocationList(Map<String, Integer> key2Freq) {
        List<CollocationDto> posCollocationList = new ArrayList<>();
        for (Map.Entry entry : key2Freq.entrySet()) {
            CollocationDto collocationDto = new CollocationDto();
            String key = (String)entry.getKey();
            int freq = (Integer) entry.getValue();
            if (!NOT_IMPORTANT.equals(key.split("_")[0])) {
                collocationDto.setFirstWord(key.split("_")[0]);
            }
            collocationDto.setFirstPos(key.split("_")[1].toUpperCase());
            if (!NOT_IMPORTANT.equals(key.split("_")[2])) {
                collocationDto.setSecondWord(key.split("_")[2]);
            }
            collocationDto.setSecondPos(key.split("_")[3].toUpperCase());
            collocationDto.setFreq(freq);
            if (key.split("_").length == 4) {
                posCollocationList.add(collocationDto);
            } else if (key.split("_").length == 6) {
                collocationDto.setThirdPos(key.split("_")[5].toUpperCase());
                posCollocationList.add(collocationDto);
            }
        }

        return posCollocationList;
    }
}
