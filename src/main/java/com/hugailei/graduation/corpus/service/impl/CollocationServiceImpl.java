package com.hugailei.graduation.corpus.service.impl;

import com.bfsuolframework.core.utils.StringUtils;
import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dao.CollocationDao;
import com.hugailei.graduation.corpus.dao.CollocationFromDictDao;
import com.hugailei.graduation.corpus.dao.CollocationWithTopicDao;
import com.hugailei.graduation.corpus.dao.WordExtensionDao;
import com.hugailei.graduation.corpus.domain.Collocation;
import com.hugailei.graduation.corpus.domain.CollocationFromDict;
import com.hugailei.graduation.corpus.domain.CollocationWithTopic;
import com.hugailei.graduation.corpus.domain.WordExtension;
import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.service.CollocationService;
import com.hugailei.graduation.corpus.util.SentencePatternUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description:
 * </p>
 **/
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class CollocationServiceImpl implements CollocationService {

    @Autowired
    private CollocationDao collocationDao;

    @Autowired
    private CollocationWithTopicDao collocationWithTopicDao;

    @Autowired
    private WordExtensionDao wordExtensionDao;

    @Autowired
    private CollocationFromDictDao collocationFromDictDao;

    private final static String NOT_IMPORTANT = "notsoimportant";

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    @Cacheable(value = "corpus", key = "#collocationDto.toString()", unless = "#result eq null")
    public List<CollocationDto> searchCollocationOfWord(CollocationDto collocationDto) {
        try {
            log.info("searchCollocationOfWord | collocationDto:{}", collocationDto.toString());
            Sort sort = new Sort(Sort.Direction.DESC, "freq");
            List<CollocationDto> resultList = new ArrayList<>();
            CollocationDto temp = new CollocationDto();
            // 不带主题的查询
            if (collocationDto.getTopic() == null ) {
                Collocation collocation = new Collocation();
                BeanUtils.copyProperties(collocationDto, collocation);
                collocationDao.findAll(Example.of(collocation), sort)
                        .forEach(coll -> {
                            BeanUtils.copyProperties(coll, temp);
                            resultList.add(temp);
                        });
            } else {
                // 带主题的查询
                CollocationWithTopic collocationWithTopic = new CollocationWithTopic();
                BeanUtils.copyProperties(collocationDto, collocationWithTopic);
                List<CollocationWithTopic> collocationWithTopicList = collocationWithTopicDao.findAll(Example.of(collocationWithTopic), sort);
                collocationWithTopicList.forEach(coll -> {
                            BeanUtils.copyProperties(coll, temp);
                            resultList.add(temp);
                        });
            }
            log.info("searchCollocationOfWord | result size: {}", (resultList != null ? resultList.size() : 0));
            return resultList;
        } catch (Exception e) {
            log.error("searchCollocationOfWord | error: {}", e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    @Cacheable(
            value = "corpus",
            key = "'synonymous_collocation_' + #collocationDto.toString()",
            unless = "#result eq null"
    )
    public List<CollocationDto> searchSynonymousCollocation(CollocationDto collocationDto) {
        try {
            log.info("searchSynonymousCollocation | collocationDto: {}", collocationDto.toString());
            // 对各单词的词性进行基本类型还原
            if (!StringUtils.isBlank(collocationDto.getFirstPos())) {
                String basePos = StanfordParserUtil.getBasePos(collocationDto.getFirstPos());
                collocationDto.setFirstPos(basePos);
            }

            if (!StringUtils.isBlank(collocationDto.getSecondPos())) {
                String basePos = StanfordParserUtil.getBasePos(collocationDto.getSecondPos());
                collocationDto.setSecondPos(basePos);
            }

            if (!StringUtils.isBlank(collocationDto.getThirdPos())) {
                String basePos = StanfordParserUtil.getBasePos(collocationDto.getThirdPos());
                collocationDto.setThirdPos(basePos);
            }

            // 设置查询所需的数据
            WordExtension wordExtension = new WordExtension();
            switch(collocationDto.getPosition()) {
                case 1:
                    if (StringUtils.isBlank(collocationDto.getFirstWord()) ||
                        StringUtils.isBlank(collocationDto.getFirstPos()) ||
                        StringUtils.isBlank(collocationDto.getSecondWord())) {
                        throw new Exception("information for search is not complete");
                    }
                    wordExtension.setWord(collocationDto.getFirstWord());
                    wordExtension.setPos(collocationDto.getFirstPos());
                    break;

                case 2:
                    if (StringUtils.isBlank(collocationDto.getSecondWord()) ||
                        StringUtils.isBlank(collocationDto.getSecondPos()) ||
                        StringUtils.isBlank(collocationDto.getFirstWord())) {
                        throw new Exception("information for search is not complete");
                    }
                    wordExtension.setWord(collocationDto.getSecondWord());
                    wordExtension.setPos(collocationDto.getSecondPos());
                    break;

                case 3:
                    if (StringUtils.isBlank(collocationDto.getThirdPos()) ||
                        StringUtils.isBlank(collocationDto.getThirdWord()) ||
                        StringUtils.isBlank(collocationDto.getFirstWord()) ||
                        StringUtils.isBlank(collocationDto.getSecondWord())) {
                        throw new Exception("information for search is not complete");
                    }
                    wordExtension.setWord(collocationDto.getThirdWord());
                    wordExtension.setPos(collocationDto.getThirdPos());
                    break;

                default:
                    throw new Exception("wrong position num");
            }

            // 查询数据库，获取待检索词在指定词性下的同义词和相似词
            wordExtension.setRelation("syn");
            Example<WordExtension> example = Example.of(wordExtension);
            Optional<WordExtension> tempResult = wordExtensionDao.findOne(example);
            String words = tempResult.isPresent() ? tempResult.get().getResults() : "";

            // 将每个同义词和相似词和原来的搭配词进行组合，进行搭配查询，看是否存在该搭配f，若存在，则放入结果集中
            List<CollocationDto> resultList = new ArrayList<>();
            CollocationWithTopic collocationWithTopic = new CollocationWithTopic();
            BeanUtils.copyProperties(collocationDto, collocationWithTopic);

            for (String word : words.split(",")) {
                if (collocationDto.getPosition() == 1) {
                    collocationWithTopic.setFirstWord(word);
                } else if (collocationDto.getPosition() == 2) {
                    collocationWithTopic.setSecondWord(word);
                } else {
                    collocationWithTopic.setThirdWord(word);
                }

                // 不带主题的查询
                if (collocationDto.getTopic() == null) {
                    Collocation collocation = new Collocation();
                    BeanUtils.copyProperties(collocationWithTopic, collocation);
                    Example<Collocation> collocationExample = Example.of(collocation);

                    List<Collocation> collocationList = collocationDao.findAll(collocationExample);
                    if (collocationList.size() >= 1) {
                        CollocationDto result = new CollocationDto();
                        collocationList.forEach(coll -> {
                            BeanUtils.copyProperties(coll, result);
                            resultList.add(result);
                        });
                    }
                }
                // 带主题的查询
                else {
                    List<CollocationWithTopic> collocationWithTopicList = collocationWithTopicDao.findAll(Example.of(collocationWithTopic));
                    if (!collocationWithTopicList.isEmpty()) {
                        CollocationDto result = new CollocationDto();
                        collocationWithTopicList.forEach(coll -> {
                            BeanUtils.copyProperties(coll, result);
                            resultList.add(result);
                        });
                    }
                }
            }
            sortCollocationDtoList(resultList);
            log.info("searchSynonymousCollocation | result size: {}", (resultList != null ? resultList.size() : 0));
            return resultList;

        } catch (Exception e) {
            log.error("searchSynonymousCollocation | error: {}", e);
            return null;
        }
    }

    @Override
    @Cacheable(value = "corpus", key = "'collocation_' + #text", unless = "#result eq null")
    public CollocationDto.CollocationInfo getCollocationInText(String text) {
        try {
            log.info("getCollocationInText | text: {}", text);
            Map<String, Integer> lemmaCollocationKey2Freq = new HashMap<>();
            Map<String, Integer> posCollocationKey2Freq = new HashMap<>();
            List<CoreMap> sentences = StanfordParserUtil.parse(text);
            for (CoreMap sentence : sentences) {
                SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
                Set<String> keyWithIndexSet = new HashSet<>();
                for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
                    String relation = edge.getRelation().toString();
                    int govIndex = edge.getGovernor().index();
                    int depIndex = edge.getDependent().index();
                    boolean found = false;
                    String firstWord = null, secondWord = null, firstPos = null, secondPos = null, thirdWord = null, thirdPos = null;
                    // firstIndex用于标记两个搭配是否为同一个单词的搭配，避免重复
                    int firstIndex = 0;
                    if (CorpusConstant.COLLOCATION_DEPENDENCY_RELATION_SET.contains(relation)) {
                        if ((relation.startsWith("nsubj") && !relation.startsWith("nsubjpass")) ||
                                "nmod:agent".equals(relation)) {
                            String adjNounRegex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                            String nounverbRegex = "((NN[A-Z]{0,1})|(PRP))-(VB[A-Z]{0,1})";
                            SentencePatternUtil.Edge temp = SentencePatternUtil.getRealNounEdge(edge.getDependent().index(), dependency);
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(adjNounRegex)) {
                                firstWord = edge.getGovernor().lemma();
                                firstIndex = edge.getGovernor().index();
                                secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                firstPos = edge.getGovernor().tag();
                                secondPos = edge.getDependent().tag();
                                found = true;
                            } else if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(nounverbRegex)) {
                                firstWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                firstIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
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
                                firstIndex = edge.getGovernor().index();
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
                                firstIndex = edge.getDependent().index();
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
                                firstIndex = edge.getDependent().index();
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
                                    firstIndex = edge.getGovernor().index();
                                    secondWord = edge.getDependent().lemma();
                                    secondPos = edge.getDependent().tag();
                                } else {
                                    firstWord = edge.getDependent().lemma();
                                    firstIndex = edge.getDependent().index();
                                    secondWord = edge.getGovernor().lemma();
                                    firstPos = edge.getDependent().tag();
                                    secondPos = edge.getGovernor().tag();
                                }
                                found = true;
                            }
                        }
                        else if ("compound:prt".equals(relation) || "nmod".equals(relation)) {
                            firstWord = edge.getGovernor().lemma();
                            firstIndex = edge.getGovernor().index();
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
                                firstIndex = edge.getGovernor().index();
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
                                            firstIndex = edge.getDependent().index();
                                            firstWord = edge.getDependent().lemma();
                                            firstPos = "JJ";

                                            int subjectIndex = semanticGraphEdge.getDependent().index();
                                            SentencePatternUtil.Edge subjectTemp = SentencePatternUtil.getRealNounEdge(subjectIndex, dependency);
                                            secondWord = (subjectTemp == null ? semanticGraphEdge.getDependent().lemma() : subjectTemp.getLemma());
                                            secondPos = "NN";
                                            found = true;
                                        }
                                    }
                                }
                            }
                        }
                        else if ("dep".equals(relation)) {
                            if (edge.getGovernor().tag().matches("VB[A-Z]{0,1}")) {
                                firstWord = edge.getGovernor().lemma();
                                firstPos = edge.getGovernor().tag();
                                firstIndex = edge.getGovernor().index();
                                secondWord = edge.getDependent().lemma();
                                secondPos = edge.getDependent().tag();
                                if (edge.getDependent().tag().startsWith("NN")) {
                                    SentencePatternUtil.Edge temp = SentencePatternUtil.getRealNounEdge(edge.getDependent().index(), dependency);
                                    secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                }
                                found = true;
                            }
                        }
                    } else if (CorpusConstant.COLLOCATION_NOMD_RELATION_SET.contains(relation)) {
                        firstWord = edge.getGovernor().lemma();
                        firstPos = edge.getGovernor().tag();
                        firstIndex = edge.getGovernor().index();
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
                        String keyWithIndex = (firstWord + "_" + firstPos + "_" + firstIndex + "_" + secondWord + "_" + secondPos).toLowerCase();
                        String key = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();
                        // 去重辅助集合
                        if (!keyWithIndexSet.contains(keyWithIndex)) {
                            keyWithIndexSet.add(keyWithIndex);
                            // 原型搭配情况统计
                            fillLemmaCollocationKey2Freq(key, lemmaCollocationKey2Freq);
                            fillPosCollocationKey2Freq(key, posCollocationKey2Freq);
                        }

                        // 统计三词搭配
                        if (!StringUtils.isBlank(thirdWord)) {
                            thirdPos = StanfordParserUtil.getBasePos(thirdPos);
                            keyWithIndex = (firstWord + "_" + firstPos + "_" + firstIndex + "_" +
                                    secondWord + "_" + secondPos + "_" +
                                    thirdWord + "_" + thirdPos).toLowerCase();
                            key = (firstWord + "_" + firstPos + "_" +
                                    secondWord + "_" + secondPos + "_" +
                                    thirdWord + "_" + thirdPos).toLowerCase();
                            if (!keyWithIndexSet.contains(keyWithIndex)) {
                                keyWithIndexSet.add(keyWithIndex);
                                fillLemmaCollocationKey2Freq(key, lemmaCollocationKey2Freq);
                                fillPosCollocationKey2Freq(key, posCollocationKey2Freq);
                            }
                        }
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
     * 验证该词对是否为正确搭配，是返回true，否则返回false
     *
     * @param wordPair
     * @return
     */
    @Override
    public Boolean checkCollocation(String wordPair) {
        try {
            log.info("checkCollocation | word pair: {}", wordPair);
            // 去除收尾及多余空格
            wordPair = wordPair.trim().replaceAll(" +", " ");
            // 先查找搭配词典
            CollocationFromDict dictCollocation = collocationFromDictDao.findOneByCollocation(wordPair);
            if (dictCollocation != null) {
                return true;
            }

            // 若词典中不存在，则查找数据库
            String[] wordArray = wordPair.split(" ");
            String firstWord = "", secondWord = "", thirdWord = "";
            if (wordArray.length == 2) {
                firstWord = wordArray[0];
                secondWord = wordArray[1];
            }
            else if (wordArray.length == 3) {
                firstWord = wordArray[0];
                secondWord = wordArray[1];
                thirdWord = wordArray[2];
            }
            List<Collocation> collocationList = collocationDao.findAllByFirstWordAndSecondWordAndThirdWordOOrderByFreqDesc(firstWord, secondWord, thirdWord);
            if (!CollectionUtils.isEmpty(collocationList)) {
                // 判断搭配出现的次数是否达到一定数量标准
                int maxFreq = collocationList.get(0).getFreq();
                if (maxFreq >= 5) {
                    return true;
                }
            }
            return false;

        } catch (Exception e) {
            log.error("checkCollocation | error: {}", e);
            return null;
        }
    }

    /**
     * 原型搭配情况统计
     *
     * @param key
     * @param key2FreqMap
     */
    private void fillLemmaCollocationKey2Freq (String key, Map<String, Integer> key2FreqMap) {
        int freq = 1;
        if (key2FreqMap.containsKey(key)) {
            freq = key2FreqMap.get(key) + 1;
        }
        key2FreqMap.put(key, freq);
    }

    /**
     * 词性搭配情况统计，在一些搭配中，忽略不重要的信息，只关注词性。
     * 如动名词的搭配中，可以忽略名词具体是哪个单词，只关注这个动词和名词词性搭配了多少次
     *
     * @param key
     * @param posCollocationKey2Freq
     */
    private void fillPosCollocationKey2Freq (String key,
                                             Map<String, Integer> posCollocationKey2Freq) {
        int freq = 1;
        String[] temp = key.split("_");
        String firstWord,  firstPos, secondWord, secondPos, thirdWord = null, thirdPos = null;
        firstWord = temp[0];
        firstPos = temp[1].toUpperCase();
        secondWord = temp[2];
        secondPos = temp[3].toUpperCase();
        String posCollocationKey;
        if (temp.length == 6) {
            thirdPos = temp[5].toUpperCase();
            freq = 1;
            posCollocationKey = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos + "_" + NOT_IMPORTANT + "_" + thirdPos).toLowerCase();
            if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
            }
            posCollocationKey2Freq.put(posCollocationKey, freq);
        } else {
            // 将所有的代词词性视为名词词性
            String posPair = firstPos + "-" + secondPos.replace("PRP", "NN");
            switch (posPair) {
                case "NN-VB":
                    posCollocationKey = (NOT_IMPORTANT + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();
                    if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                        freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
                    }
                    posCollocationKey2Freq.put(posCollocationKey, freq);
                    break;

                case "VB-NN":
                case "JJ-NN":
                case "VB-IN":
                case "VB-RP":
                case "NN-IN":
                case "JJ-IN":
                    posCollocationKey = (firstWord + "_" + firstPos + "_" + NOT_IMPORTANT + "_" + secondPos).toLowerCase();
                    if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                        freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
                    }
                    posCollocationKey2Freq.put(posCollocationKey, freq);
                    break;

                case "VB-RB":
                case "RB-VB":
                case "JJ-RB":
                case "RB-JJ":
                case "VB-JJ":
                    posCollocationKey = (firstWord + "_" + firstPos + "_" + NOT_IMPORTANT + "_" + secondPos).toLowerCase();
                    if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                        freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
                    }
                    posCollocationKey2Freq.put(posCollocationKey, freq);

                    posCollocationKey = (NOT_IMPORTANT + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();
                    if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                        freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
                    }
                    posCollocationKey2Freq.put(posCollocationKey, freq);
                    break;

                default:
                    break;
            }// switch
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

    /**
     * 对collocationDto的列表进行降序排序
     *
     * @param collocationDtoList
     */
    static void sortCollocationDtoList(List<CollocationDto> collocationDtoList) {
        Collections.sort(collocationDtoList, new Comparator<CollocationDto>() {
            @Override
            public int compare(CollocationDto c1, CollocationDto c2) {
                return c2.getFreq() - c1.getFreq();
            }
        });
    }
}
