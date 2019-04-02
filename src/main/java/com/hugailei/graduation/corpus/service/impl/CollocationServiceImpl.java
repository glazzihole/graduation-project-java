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
import com.hugailei.graduation.corpus.service.StudentRankWordService;
import com.hugailei.graduation.corpus.util.SentenceAnalysisUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private StudentRankWordService studentRankWordService;

    private final static String NOT_IMPORTANT = "notsoimportant";

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    @Cacheable(value = "corpus", key = "#collocationDto.toString()", unless = "#result eq null")
    public List<CollocationDto> searchCollocationOfWord(CollocationDto collocationDto, HttpServletRequest request) {
        try {
            log.info("searchCollocationOfWord | collocationDto:{}", collocationDto.toString());
            Sort sort = new Sort(Sort.Direction.DESC, "freq");
            List<CollocationDto> resultList = new ArrayList<>();
            // 不带主题的查询
            if (collocationDto.getTopic() == null) {
                Collocation collocation = new Collocation();
                BeanUtils.copyProperties(collocationDto, collocation);
                collocationDao.findAll(Example.of(collocation), sort)
                        .forEach(coll -> {
                            CollocationDto temp = new CollocationDto();
                            BeanUtils.copyProperties(coll, temp);
                            resultList.add(temp);
                        });
            } else {
                // 带主题的查询
                CollocationWithTopic collocationWithTopic = new CollocationWithTopic();
                BeanUtils.copyProperties(collocationDto, collocationWithTopic);
                List<CollocationWithTopic> collocationWithTopicList = collocationWithTopicDao.findAll(Example.of(collocationWithTopic), sort);
                collocationWithTopicList.forEach(coll -> {
                            CollocationDto temp = new CollocationDto();
                            BeanUtils.copyProperties(coll, temp);
                            resultList.add(temp);
                        });
            }
            log.info("searchCollocationOfWord | result size: {}", (resultList != null ? resultList.size() : 0));
            if (collocationDto.getRankNum() != null) {
                HttpSession session = request.getSession();
                Long studentId = (Long)session.getAttribute("studentId");
                labelWord(resultList, collocationDto.getRankNum(), studentId);
            }
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
    public List<CollocationDto> searchSynonymousCollocation(CollocationDto collocationDto,
                                                            HttpServletRequest request) {
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

            // 将每个同义词和相似词和原来的搭配词进行组合，进行搭配查询，看是否存在该搭配，若存在，则放入结果集中
            List<CollocationDto> resultListFromCorpus = new ArrayList<>();
            List<CollocationDto> resultListFromDict = new ArrayList<>();
            CollocationWithTopic collocationWithTopic = new CollocationWithTopic();
            BeanUtils.copyProperties(collocationDto, collocationWithTopic);

            // 先根据搭配中的非变动部分进行一次查询，后续查询在此次查询的基础上进行子查询，提升效率
            if (collocationDto.getPosition() == 1) {
                collocationWithTopic.setFirstWord(null);
            } else if (collocationDto.getPosition() == 2) {
                collocationWithTopic.setSecondWord(null);
            } else {
                collocationWithTopic.setThirdWord(null);
            }
            List<Collocation> tempCollocationList = new ArrayList<>();
            List<CollocationWithTopic> tempCollocationWithTopic = new ArrayList<>();
            // 未指定主题的查询
            if (collocationDto.getTopic() == null) {
                Collocation collocation = new Collocation();
                BeanUtils.copyProperties(collocationWithTopic, collocation);
                Example<Collocation> collocationExample = Example.of(collocation);
                tempCollocationList = collocationDao.findAll(collocationExample);
            }
            // 指定了查询主题
            else {
                Example<CollocationWithTopic> collocationWithTopicExample = Example.of(collocationWithTopic);
                tempCollocationWithTopic = collocationWithTopicDao.findAll(collocationWithTopicExample);
            }

            for (String word : words.split(",")) {
                if (collocationDto.getPosition() == 1) {
                    if (word.equals(collocationDto.getFirstWord())) {
                        continue;
                    }
                    collocationWithTopic.setFirstWord(word);
                } else if (collocationDto.getPosition() == 2) {
                    if (word.equals(collocationDto.getSecondWord())) {
                        continue;
                    }
                    collocationWithTopic.setSecondWord(word);
                } else {
                    if (word.equals(collocationDto.getThirdWord())) {
                        continue;
                    }
                    collocationWithTopic.setThirdWord(word);
                }

                // 不带主题的查询
                if (collocationDto.getTopic() == null) {
                    // 若未指定语料库，则先查询搭配词典，若搭配词典中查询不到再查询语料库
                    if (collocationDto.getCorpus() == null) {
                        String collocation = collocationWithTopic.getFirstWord() + " " + collocationWithTopic.getSecondWord();
                        if (StringUtils.isBlank(collocationWithTopic.getThirdWord())) {
                            collocation = collocation + " " + collocationWithTopic.getThirdWord();
                        }
                        CollocationFromDict collocationFromDict = collocationFromDictDao.findFirstByCollocation(collocation);
                        if (collocationFromDict != null) {
                            CollocationDto result = new CollocationDto();
                            String[] wordArray = collocationFromDict.getCollocation().split(" ");
                            if (wordArray.length == 2) {
                                result.setFirstWord(wordArray[0]);
                                result.setSecondWord(wordArray[1]);
                            }
                            else if (wordArray.length == 3) {
                                result.setFirstWord(wordArray[0]);
                                result.setSecondWord(wordArray[1]);
                                result.setThirdWord(wordArray[2]);
                            }
                            result.setCorpus("Oxford Collocations Dictionary");
                            resultListFromDict.add(result);
                            continue;
                        }
                    }
                    // 在上面templist中进行子查询
                    for (Collocation collocation : tempCollocationList) {
                        String synWord;
                        if (collocationDto.getPosition() == 1) {
                            synWord = collocation.getFirstWord();
                        } else if (collocationDto.getPosition() == 2) {
                            synWord = collocation.getSecondWord();
                        } else {
                            synWord = collocation.getThirdWord();
                        }
                        if (word.equals(synWord)) {
                            CollocationDto coll = new CollocationDto();
                            BeanUtils.copyProperties(collocation, coll);
                            resultListFromCorpus.add(coll);
                        }
                    }
                }
                // 带主题的查询
                else {
                    // 在上面templist中进行子查询
                    for (CollocationWithTopic collWithTopic : tempCollocationWithTopic) {
                        String synWord;
                        if (collocationDto.getPosition() == 1) {
                            synWord = collWithTopic.getFirstWord();
                        } else if (collocationDto.getPosition() == 2) {
                            synWord = collWithTopic.getSecondWord();
                        } else {
                            synWord = collWithTopic.getThirdWord();
                        }
                        if (word.equals(synWord)) {
                            CollocationDto coll = new CollocationDto();
                            BeanUtils.copyProperties(collWithTopic, coll);
                            resultListFromCorpus.add(coll);
                        }
                    }
                }
            }
            List<CollocationDto> resultList = new ArrayList<>();
            sortCollocationDtoList(resultListFromCorpus);
            resultList.addAll(resultListFromDict);
            resultList.addAll(resultListFromCorpus);
            log.info("searchSynonymousCollocation | result size: {}", (resultList != null ? resultList.size() : 0));
            if (collocationDto.getRankNum() != null) {
                Long studentId = (Long)request.getSession().getAttribute("studentId");
                labelWord(resultList, collocationDto.getRankNum(), studentId);
            }
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
            Set<String> posKeyWithIndexSet = new HashSet<>();
            List<CoreMap> sentences = StanfordParserUtil.parse(text);
            int sentenceNum = 0;
            for (CoreMap sentence : sentences) {
                sentenceNum ++;
                SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
                Set<String> keyWithIndexSet = new HashSet<>();
                for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
                    String relation = edge.getRelation().toString();
                    int govIndex = edge.getGovernor().index();
                    int depIndex = edge.getDependent().index();
                    boolean found = false;
                    String firstWord = null, secondWord = null, firstPos = null, secondPos = null, thirdWord = null, thirdPos = null;
                    // firstIndex用于标记两个搭配是否为同一个单词的搭配，避免重复
                    int firstIndex = 0, secondIndex = 0, thirdIndex = 0;
                    if (CorpusConstant.COLLOCATION_DEPENDENCY_RELATION_SET.contains(relation)) {
                        if ((relation.startsWith("nsubj") && !relation.startsWith("nsubjpass")) ||
                                "nmod:agent".equals(relation)) {
                            String adjNounRegex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                            String nounverbRegex = "((NN[A-Z]{0,1})|(PRP))-(VB[A-Z]{0,1})";
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(adjNounRegex)) {
                                firstWord = edge.getGovernor().lemma();
                                firstIndex = edge.getGovernor().index();
                                secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                secondIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
                                firstPos = edge.getGovernor().tag();
                                secondPos = edge.getDependent().tag();
                                found = true;
                            } else if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(nounverbRegex)) {
                                firstWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                firstIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
                                secondWord = edge.getGovernor().lemma();
                                firstPos = edge.getDependent().tag();
                                secondPos = edge.getGovernor().tag();
                                secondIndex = edge.getGovernor().index();
                                found = true;
                            }
                        }
                        else if (relation.startsWith("dobj") || relation.startsWith("nsubjpass")) {
                            String verbNounRegex = "(VB[A-Z]{0,1})-((NN[A-Z]{0,1})|(PRP))";
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbNounRegex)) {
                                firstWord = edge.getGovernor().lemma();
                                firstIndex = edge.getGovernor().index();
                                firstPos = edge.getGovernor().tag();
                                secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                secondPos = edge.getDependent().tag();
                                secondIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
                                found = true;
                            }
                            else {
                                firstWord = edge.getGovernor().lemma();
                                firstPos = edge.getGovernor().tag();
                                firstIndex = edge.getGovernor().index();
                                secondWord = edge.getDependent().lemma();
                                secondPos = edge.getDependent().tag();
                                secondIndex = edge.getDependent().index();
                                found = true;
                            }
                        }
                        else if (relation.startsWith("csubj")) {
                            String verbNounRegex = "(VB[A-Z]{0,1})-((NN[A-Z]{0,1})|(PRP))";
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getGovernor().index(), sentence);
                            if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(verbNounRegex)) {
                                firstWord = edge.getDependent().lemma();
                                firstIndex = edge.getDependent().index();
                                secondWord = (temp == null ? edge.getGovernor().lemma() : temp.getLemma());
                                firstPos = edge.getDependent().tag();
                                secondPos = edge.getGovernor().tag();
                                secondIndex = (temp == null ? edge.getGovernor().index() : temp.getIndex());
                                found = true;
                            }
                        }
                        else if (relation.startsWith("amod")) {
                            String adjNounRegex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getGovernor().index(), sentence);
                            if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(adjNounRegex)) {
                                firstWord = edge.getDependent().lemma();
                                firstIndex = edge.getDependent().index();
                                firstPos = edge.getDependent().tag();
                                secondWord = (temp == null ? edge.getGovernor().lemma() : temp.getLemma());
                                secondPos = edge.getGovernor().tag();
                                secondIndex = (temp == null ? edge.getGovernor().index() : temp.getIndex());
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
                                    secondIndex = edge.getDependent().index();
                                } else {
                                    firstWord = edge.getDependent().lemma();
                                    firstIndex = edge.getDependent().index();
                                    secondWord = edge.getGovernor().lemma();
                                    firstPos = edge.getDependent().tag();
                                    secondPos = edge.getGovernor().tag();
                                    secondIndex = edge.getGovernor().index();
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
                            secondIndex = edge.getDependent().index();
                            found = true;
                        }
                        else if ("compound".equals(relation)) {
                            if (edge.getDependent().tag().matches("NN.*") && edge.getGovernor().tag().matches("NN.*")) {
                                int governorIndex = edge.getGovernor().index();
                                int dependentIndex = edge.getDependent().index();
                                CoreLabel governorToken = sentence.get(CoreAnnotations.TokensAnnotation.class).get(governorIndex - 1);
                                String governorNer = governorToken.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                                CoreLabel dependentToken = sentence.get(CoreAnnotations.TokensAnnotation.class).get(dependentIndex - 1);
                                String dependetNer = dependentToken.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                                if (!(CorpusConstant.PROPER_NOUN_SET.contains(governorNer) && CorpusConstant.PROPER_NOUN_SET.contains(dependetNer))) {
                                    found = true;
                                    firstWord = edge.getDependent().lemma();
                                    firstPos = "NN";
                                    firstIndex = edge.getDependent().index();
                                    secondWord = edge.getGovernor().lemma();
                                    secondPos = "NN";
                                    secondIndex = edge.getGovernor().index();
                                }
                            }
                        }
                        else if (relation.startsWith("xcomp")) {
                            String verbAdjRegex = "(VB[A-Z]{0,1})-(JJ[A-Z]{0,1})";
                            String verbNounRegex = "(VB[A-Z]{0,1})-((NN[A-Z]{0,1})|(PRP))";
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbAdjRegex) ||
                                    (edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbNounRegex)) {
                                SentenceAnalysisUtil.Edge temp = null;
                                if (edge.getDependent().tag().startsWith("NN")) {
                                    temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                                }
                                firstWord = edge.getGovernor().lemma();
                                firstIndex = edge.getGovernor().index();
                                secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                secondPos = edge.getDependent().tag();
                                firstPos = edge.getGovernor().tag();
                                secondIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
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
                                            firstIndex = edge.getDependent().index();
                                            int subjectIndex = semanticGraphEdge.getDependent().index();
                                            SentenceAnalysisUtil.Edge subjectTemp = SentenceAnalysisUtil.getRealNounEdge(subjectIndex, sentence);
                                            secondWord = (subjectTemp == null ? semanticGraphEdge.getDependent().lemma() : subjectTemp.getLemma());
                                            secondPos = "NN";
                                            secondIndex = (subjectTemp == null ? semanticGraphEdge.getDependent().index() : subjectTemp.getIndex());
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
                                secondIndex = edge.getDependent().index();
                                if (edge.getDependent().tag().startsWith("NN")) {
                                    SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                                    secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                    secondIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
                                }
                                found = true;
                            }
                        }
                        else if ("mwe".equals(relation)) {
                            found = true;
                            firstWord = edge.getGovernor().lemma();
                            firstPos = edge.getGovernor().tag();
                            firstIndex = edge.getGovernor().index();
                            secondWord = edge.getDependent().lemma();
                            secondPos = edge.getDependent().tag();
                            secondIndex = edge.getDependent().index();
                        }
                    } else if (CorpusConstant.COLLOCATION_NOMD_RELATION_SET.contains(relation)) {
                        firstWord = edge.getGovernor().lemma();
                        firstPos = edge.getGovernor().tag();
                        firstIndex = edge.getGovernor().index();
                        secondWord = relation.split(":")[1];
                        secondPos = "IN";
                        thirdWord = edge.getDependent().lemma();
                        thirdPos = edge.getDependent().tag();
                        thirdIndex = edge.getDependent().index();
                        if (thirdPos.startsWith("NN")) {
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                            if (temp != null) {
                                thirdWord = temp.getLemma();
                            }
                        }
                        found = true;
                    }

                    if (found) {
                        // 查询搭配中的动词是否存在词组搭配，若存在，则需要将所有搭配中的该动词替换为词组
//                        if (
//                                (firstPos.matches("VB.*") || secondPos.matches("VB.*"))
//                                        &&
//                                        (!relation.equals("compound:prt"))
//                        ) {
                        if (!relation.equals("compound:prt")) {
                            for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                                if (e.getGovernor().index() == firstIndex && e.getRelation().toString().equals("compound:prt")) {
                                    String phrase = e.getGovernor().lemma() + " " + e.getDependent().lemma();
                                    firstWord = phrase;
                                    firstPos = "PHRASE";
                                }
                            }

                            for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                                if (e.getGovernor().index() == secondIndex && e.getRelation().toString().equals("compound:prt")) {
                                    String phrase = e.getGovernor().lemma() + " " + e.getDependent().lemma();
                                    secondWord = phrase;
                                    secondPos = "PHRASE";
                                }
                            }

                            for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                                if (e.getGovernor().index() == thirdIndex && e.getRelation().toString().equals("compound:prt")) {
                                    String phrase = e.getGovernor().lemma() + " " + e.getDependent().lemma();
                                    thirdWord = phrase;
                                    thirdPos = "PHRASE";
                                }
                            }
                        }

                        // 词性同一存储为该词性下原型的词性
                        firstPos = StanfordParserUtil.getBasePos(firstPos);
                        secondPos = StanfordParserUtil.getBasePos(secondPos);
                        String keyWithIndex = (firstWord + "_" + firstPos + "_" + firstIndex + "_" +
                                secondWord + "_" + secondPos + "_" + secondIndex).toLowerCase();
                        String key = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();
                        // 去重辅助集合
                        if (!keyWithIndexSet.contains(keyWithIndex)) {
                            keyWithIndexSet.add(keyWithIndex);
                            // 原型搭配情况统计
                            fillLemmaCollocationKey2Freq(key, lemmaCollocationKey2Freq);
                            fillPosCollocationKey2Freq(keyWithIndex, sentenceNum, posCollocationKey2Freq, posKeyWithIndexSet);
                        }

                        // 统计三词搭配
                        if (!StringUtils.isBlank(thirdWord)) {
                            thirdPos = StanfordParserUtil.getBasePos(thirdPos);
                            keyWithIndex = (firstWord + "_" + firstPos + "_" + firstIndex + "_" +
                                    secondWord + "_" + secondPos + "_" + secondIndex + "_" +
                                    thirdWord + "_" + thirdPos + "_" + thirdIndex).toLowerCase();
                            key = (firstWord + "_" + firstPos + "_" +
                                    secondWord + "_" + secondPos + "_" +
                                    thirdWord + "_" + thirdPos).toLowerCase();
                            if (!keyWithIndexSet.contains(keyWithIndex)) {
                                keyWithIndexSet.add(keyWithIndex);
                                fillLemmaCollocationKey2Freq(key, lemmaCollocationKey2Freq);
                                fillPosCollocationKey2Freq(keyWithIndex, sentenceNum, posCollocationKey2Freq, posKeyWithIndexSet);
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
    @Cacheable(value = "corpus", key = "'collocation_check' + #wordPair", unless = "#result eq null")
    @Override
    public Boolean checkCollocation(String wordPair) {
        try {
            log.info("checkCollocation | word pair: {}", wordPair);
            // 去除收尾及多余空格
            wordPair = wordPair.trim().replaceAll(" +", " ");
            // 先查找搭配词典
            CollocationFromDict dictCollocation = collocationFromDictDao.findFirstByCollocation(wordPair);
            if (dictCollocation != null) {
                return true;
            }

            // 若词典中不存在，则查找数据库
            List<Collocation> collocationList = collocationDao.findAllByWordPairOrderByFreqDesc(wordPair);
            if (!CollectionUtils.isEmpty(collocationList)) {
                // 判断搭配出现的次数是否达到一定数量标准
                int maxFreq = collocationList.get(0).getFreq();
                if (maxFreq >= 3) {
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
     * 验证多个词对是否为正确搭配
     *
     * @param wordPairList
     * @return
     */
    @Override
    public List<Boolean> checkCollocationList(String wordPairList) {
        try {
            log.info("checkCollocationList | word pair list: {}", wordPairList);
            // 去除收尾及多余空格
            wordPairList = wordPairList.trim().replaceAll(" +", " ");
            List<Boolean> resultList = new ArrayList<>();
            for (String wordPair : wordPairList.split(",")) {
                // 先查找搭配词典
                CollocationFromDict dictCollocation = collocationFromDictDao.findFirstByCollocation(wordPair);
                if (dictCollocation != null) {
                    // 若词典中不存在，则查找数据库
                    List<Collocation> collocationList = collocationDao.findAllByWordPairOrderByFreqDesc(wordPair);
                    if (!CollectionUtils.isEmpty(collocationList)) {
                        // 判断搭配出现的次数是否达到一定数量标准
                        int maxFreq = collocationList.get(0).getFreq();
                        if (maxFreq >= 3) {
                            resultList.add(true);
                            break;
                        }
                    }
                    resultList.add(false);
                } else {
                    resultList.add(true);
                }
            }
            return resultList;
        } catch (Exception e) {
            log.error("checkCollocationList | error: {}", e);
            return null;
        }
    }

    /**
     * 同义搭配推荐，暂时只对二词搭配作推荐
     *
     * @param wordPair
     * @param posPair
     * @param corpus
     * @param rankNum
     * @param topic
     * @param request
     * @return
     */
    @Cacheable(
            value = "corpus",
            key = "'collocation_syn_recommend' + #wordPair + '-' + #posPair + '-' + #corpus + '-' + #rankNum + '-' + #topic",
            unless = "#result eq null")
    @Override
    public List<CollocationDto> recommendSynonym(String wordPair,
                                                 String posPair,
                                                 String corpus,
                                                 Integer rankNum,
                                                 Integer topic,
                                                 HttpServletRequest request) {
        try {
            log.info("recommendSynonym | words: {}, pos: {}, corpus: {}, rank num: {}, topic: {}", wordPair, posPair, corpus, rankNum, topic);
            // 仅支持二词搭配
            if (wordPair.split(" +").length != 2 || (!StringUtils.isBlank(posPair) && posPair.split(" +").length != 2)) {
                throw new Exception("only two-words' collocation is supported");
            }
            // 获取搭配中各词的原型和词性
            List<CoreMap> coreMapList = StanfordParserUtil.parse(wordPair);
            String firstWord = coreMapList
                    .get(0)
                    .get(CoreAnnotations.TokensAnnotation.class)
                    .get(0)
                    .get(CoreAnnotations.LemmaAnnotation.class);
            String secondWord = coreMapList
                    .get(0)
                    .get(CoreAnnotations.TokensAnnotation.class)
                    .get(1)
                    .get(CoreAnnotations.LemmaAnnotation.class);
            String firstWordPos = coreMapList
                    .get(0)
                    .get(CoreAnnotations.TokensAnnotation.class)
                    .get(0)
                    .get(CoreAnnotations.PartOfSpeechAnnotation.class);
            String secondWordPos = coreMapList
                    .get(0)
                    .get(CoreAnnotations.TokensAnnotation.class)
                    .get(1)
                    .get(CoreAnnotations.PartOfSpeechAnnotation.class);
            if (!StringUtils.isBlank(posPair)) {
                firstWordPos = posPair.split(" ")[0];
                secondWordPos = posPair.split(" ")[1];
            }
            String wordPairPos = firstWordPos + "-" + secondWordPos;
            // 根据词性组合情况获取同义搭配
            List<CollocationDto> resultList = new ArrayList<>();
            CollocationDto collocationDto = new CollocationDto();
            collocationDto.setFirstPos(firstWordPos);
            collocationDto.setFirstWord(firstWord);
            collocationDto.setSecondPos(secondWordPos);
            collocationDto.setSecondWord(secondWord);
            collocationDto.setTopic(topic);
            collocationDto.setCorpus(corpus);
            collocationDto.setRankNum(rankNum);
            // 形容词 + 名词、动词 + 名词、副词 + 形容词、动词 + 副词、副词 + 动词的组合，查找第一个词的同义词
            if (wordPairPos.matches("JJ-NN") ||
                wordPairPos.matches("VB-NN") ||
                wordPairPos.matches("RB-JJ") ||
                wordPairPos.matches("VB-RB") ||
                wordPairPos.matches("RB-VB") ||
                wordPairPos.matches("JJ-IN")) {
                collocationDto.setPosition(1);
            }
            // 名词 + 动词、副词 + 形容词、动词 + 副词、副词 + 动词的组合，查找第二个词的同义词
            else if (wordPairPos.matches("NN-VB")||
                    wordPairPos.matches("RB-JJ") ||
                    wordPairPos.matches("VB-RB") ||
                    wordPairPos.matches("RB-VB")) {
                collocationDto.setPosition(2);
            }
            else if (wordPairPos.matches("VB-JJ") ||
                    wordPairPos.matches("NN-NN")) {
                collocationDto.setPosition(1);
                List<CollocationDto> temp = searchSynonymousCollocation(collocationDto, request);
                if (temp != null) {
                    resultList.addAll(temp);
                }
                collocationDto.setPosition(2);
                temp = searchSynonymousCollocation(collocationDto, request);
                if (temp != null) {
                    resultList.addAll(temp);
                }
                sortCollocationDtoList(resultList);
                return resultList;
            }
            List<CollocationDto> temp = searchSynonymousCollocation(collocationDto, request);
            if (temp != null) {
                resultList.addAll(temp);
            }
            return resultList;
        } catch (Exception e) {
            log.error("recommendSynonym | error: {}", e);
            return null;
        }
    }

    /**
     * 查找搭配词典中的搭配，查找单词在搭配词典中的搭配，按照词性、搭配词词性及搭配分类
     *
     * @param word
     * @param rankNum
     * @return
     */
    @Cacheable(value = "corpus", key = "'collocation_search_dict' + #word + '_' + #rankNum", unless = "#result eq null")
    @Override
    public List<CollocationDto.CollocationDictInfo> searchCollocationInDict(String word, Integer rankNum) {
        try {
            log.info("searchCollocationInDict | word: {}, rank num: {}", word, rankNum);
            List<CollocationFromDict> collocationFromDictList = collocationFromDictDao.findAllByWord(word);
            Map<String, Map<String, Set<String>>> pos2collocationPos2Collocation = new LinkedHashMap<>();
            for (CollocationFromDict collocationInfo : collocationFromDictList) {
                String pos = collocationInfo.getPos();
                String collocationPos = collocationInfo.getCollocationPos();
                String collocation = collocationInfo.getCollocation();
                if (pos2collocationPos2Collocation.containsKey(pos)) {
                    Map<String, Set<String>> collocationPos2Collocation = pos2collocationPos2Collocation.get(pos);
                    if (collocationPos2Collocation.containsKey(collocationPos)) {
                        Set<String> collocationSet = collocationPos2Collocation.get(collocationPos);
                        collocationSet.add(collocation);
                        collocationPos2Collocation.put(collocationPos, collocationSet);
                        pos2collocationPos2Collocation.put(pos, collocationPos2Collocation);
                    } else {
                        Set<String> collocationSet = new LinkedHashSet<>();
                        collocationSet.add(collocation);
                        collocationPos2Collocation.put(collocationPos, collocationSet);
                        pos2collocationPos2Collocation.put(pos, collocationPos2Collocation);
                    }
                } else {
                    Map<String, Set<String>> collocationPos2Collocation = new LinkedHashMap<>();
                    Set<String> collocationSet = new LinkedHashSet<>();
                    collocationSet.add(collocation);
                    collocationPos2Collocation.put(collocationPos, collocationSet);
                    pos2collocationPos2Collocation.put(pos, collocationPos2Collocation);
                }
            }
            List<CollocationDto.CollocationDictInfo> collocationDictInfoList = new ArrayList<>();
            for (Map.Entry entry : pos2collocationPos2Collocation.entrySet()) {
                String wordPos = (String)entry.getKey();
                Map<String, Set<String>>  collocationPos2Collocation = (Map<String, Set<String>>)entry.getValue();
                CollocationDto.CollocationDictInfo collocationDictInfo = new CollocationDto.CollocationDictInfo();
                collocationDictInfo.setWordPos(wordPos);
                List<CollocationDto.CollocationWordInfo> collocationWordInfoList = new ArrayList<>();
                for (Map.Entry collocationEntry : collocationPos2Collocation.entrySet()) {
                    String collocationPos = (String)collocationEntry.getKey();
                    Set<String> collocationSet = (Set<String>)collocationEntry.getValue();
                    List<String> collocationList = new ArrayList<>(collocationSet);
                    CollocationDto.CollocationWordInfo collocationWordInfo = new CollocationDto.CollocationWordInfo();
                    collocationWordInfo.setCollocationPos(collocationPos);
                    collocationWordInfo.setCollocationList(collocationList);
                    collocationWordInfoList.add(collocationWordInfo);
                }
                collocationDictInfo.setCollocationWordInfoList(collocationWordInfoList);
                collocationDictInfoList.add(collocationDictInfo);
            }
            if (rankNum != null) {
                Set<String> rankWordSet = CorpusConstant.RANK_NUM_TO_DIFFICULT_WORD_SET.get(rankNum);
                for (int i = 0; i < collocationDictInfoList.size(); i++) {
                    CollocationDto.CollocationDictInfo collocationDictInfo = collocationDictInfoList.get(i);
                    List<CollocationDto.CollocationWordInfo> collocationWordInfoList = collocationDictInfo.getCollocationWordInfoList();
                    for (int j = 0; j < collocationWordInfoList.size(); j++) {
                        CollocationDto.CollocationWordInfo collocationWordInfo = collocationWordInfoList.get(j);
                        List<String> collocationList = collocationWordInfo.getCollocationList();
                        for (int k = 0; k < collocationList.size(); k++) {
                            String collocationString = collocationList.get(k);
                            String labeledCollocationString = "";
                            for (String collocationWord : collocationString.split(" ")) {
                                if (rankWordSet.contains(collocationWord)) {
                                    collocationWord = CorpusConstant.RANK_WORD_STRENGTHEN_OPEN_LABEL + collocationWord + CorpusConstant.RANK_WORD_STRENGTHEN_CLOSE_LABEL;
                                }
                                labeledCollocationString = labeledCollocationString + collocationWord + " ";
                            }
                            labeledCollocationString = labeledCollocationString.trim();
                            collocationList.set(k, labeledCollocationString);
                        }
                        collocationWordInfo.setCollocationList(collocationList);
                        collocationWordInfoList.set(j, collocationWordInfo);
                    }
                    collocationDictInfo.setCollocationWordInfoList(collocationWordInfoList);
                    collocationDictInfoList.set(i, collocationDictInfo);
                }
            }
            return collocationDictInfoList;
        } catch (Exception e) {
            log.error("searchCollocationInDict | error: {}", e);
            return null;
        }
    }

    /**
     * 搭配主题分布统计
     *
     * @param word_pair
     * @param corpus
     * @return
     */
    @Override
    public List<CollocationDto> topicDistribution(String word_pair, String corpus) {
        try {
            log.info("topicDistribution | word pair: {}, corpus: {}", word_pair, corpus);
            // 替换多余空格
            word_pair = word_pair.replaceAll(" +", " ");
            List<CollocationDto> collocationDtoList = collocationWithTopicDao
                    .findByWordPairAndCorpus(word_pair, corpus)
                    .stream()
                    .map(c -> {
                        CollocationDto cDto = new CollocationDto();
                        cDto.setCorpus(corpus);
                        cDto.setTopic(c.getTopic());
                        cDto.setFreq(c.getFreq());
                        return cDto;
                    })
                    .collect(Collectors.toList());
            return collocationDtoList;
        } catch (Exception e) {
            log.error("topicDistribution | error: {}", e);
            return null;
        }
    }

    /**
     * 搭配的语料库分布
     *
     * @param word_pair
     * @return
     */
    @Override
    public List<CollocationDto> corpusDistribution(String word_pair) {
        try {
            log.info("corpusDistribution | word pair: {}", word_pair);
            List<CollocationDto> collocationDtoList = collocationDao
                    .findByWordPair(word_pair)
                    .stream()
                    .map(c -> {
                        CollocationDto cDto = new CollocationDto();
                        cDto.setCorpus(c.getCorpus());
                        cDto.setFreq(c.getFreq());
                        return cDto;
                    })
                    .collect(Collectors.toList());
            return collocationDtoList;
        } catch (Exception e) {
            log.error("corpusDistribution | error: {}", e);
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
     * @param sentenceNum   记录当前搭配所在的句子标号
     * @param posCollocationKey2Freq
     * @param keyWithIndexSet    记录主导词的位置，避免同一个词修饰了不同词被识别为重复搭配的情况
     */
    private void fillPosCollocationKey2Freq (String key,
                                             int sentenceNum,
                                             Map<String, Integer> posCollocationKey2Freq,
                                             Set<String> keyWithIndexSet) {
        int freq = 1;
        String[] temp = key.split("_");
        String firstWord,  firstPos, secondWord, secondPos, thirdPos;
        int firstWordIndex = 0, secondWordIndex = 0;
        firstWord = temp[0];
        firstPos = temp[1].toUpperCase();
        firstWordIndex = Integer.valueOf(temp[2]);
        secondWord = temp[3];
        secondPos = temp[4].toUpperCase();
        secondWordIndex = Integer.valueOf(temp[5]);

        String posCollocationKey, lemmaWithIndex;
        if (temp.length == 9) {
            thirdPos = temp[7].toUpperCase();
            freq = 1;
            posCollocationKey = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos + "_" + NOT_IMPORTANT + "_" + thirdPos).toLowerCase();
            lemmaWithIndex = (firstWord + "_" + firstPos + "_" + sentenceNum + "." + firstWordIndex + "_" +
                    secondWord + "_" + secondPos + "_" + sentenceNum + "." + secondWordIndex + "_" +
                    NOT_IMPORTANT + "_" + thirdPos).toLowerCase();
            if (!keyWithIndexSet.contains(lemmaWithIndex)) {
                if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                    freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
                }
                posCollocationKey2Freq.put(posCollocationKey, freq);
                keyWithIndexSet.add(lemmaWithIndex);
            }
        } else {
            // 将所有的代词词性视为名词词性
            String posPair = firstPos + "-" + secondPos.replace("PRP", "NN");
            switch (posPair) {
                case "NN-VB":
                case "PRP-VB":
                    posCollocationKey = (NOT_IMPORTANT + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();
                    lemmaWithIndex = (NOT_IMPORTANT + "_" + firstPos + "_" + secondWord + "_" + secondPos + "_" + sentenceNum + "." + secondWordIndex).toLowerCase();
                    if (!keyWithIndexSet.contains(lemmaWithIndex)) {
                        if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                            freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
                        }
                        posCollocationKey2Freq.put(posCollocationKey, freq);
                        keyWithIndexSet.add(lemmaWithIndex);
                    }
                    break;

                case "VB-NN":
                case "JJ-NN":
                case "VB-IN":
                case "VB-RP":
                case "NN-IN":
                case "JJ-IN":
                    posCollocationKey = (firstWord + "_" + firstPos + "_" + NOT_IMPORTANT + "_" + secondPos).toLowerCase();
                    lemmaWithIndex = (firstWord + "_" + firstPos + "_" + sentenceNum + "." + firstWordIndex + "_" + NOT_IMPORTANT + "_" + secondPos).toLowerCase();
                    if (!keyWithIndexSet.contains(lemmaWithIndex)) {
                        if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                            freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
                        }
                        posCollocationKey2Freq.put(posCollocationKey, freq);
                        keyWithIndexSet.add(lemmaWithIndex);
                    }
                    break;

                case "VB-RB":
                case "RB-VB":
                case "JJ-RB":
                case "RB-JJ":
                case "VB-JJ":
                    posCollocationKey = (firstWord + "_" + firstPos + "_" + NOT_IMPORTANT + "_" + secondPos).toLowerCase();
                    lemmaWithIndex = (firstWord + "_" + firstPos + "_" + sentenceNum + "." + firstWordIndex + "_" + NOT_IMPORTANT + "_" + secondPos).toLowerCase();
                    if (!keyWithIndexSet.contains(lemmaWithIndex)) {
                        if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                            freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
                        }
                        posCollocationKey2Freq.put(posCollocationKey, freq);
                        keyWithIndexSet.add(lemmaWithIndex);
                    }

                    posCollocationKey = (NOT_IMPORTANT + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();
                    lemmaWithIndex = (NOT_IMPORTANT + "_" + firstPos + "_" + secondWord + "_" + secondPos + "_" + sentenceNum + "." + secondWordIndex).toLowerCase();
                    if (!keyWithIndexSet.contains(lemmaWithIndex)) {
                        if (posCollocationKey2Freq.containsKey(posCollocationKey)) {
                            freq = posCollocationKey2Freq.get(posCollocationKey) + 1;
                        }
                        posCollocationKey2Freq.put(posCollocationKey, freq);
                        keyWithIndexSet.add(lemmaWithIndex);
                    }
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

    /**
     * 对检索内容进行重构标注
     *
     * @param result
     * @param rankNum
     * @param studentId
     */
    private void labelWord(List<CollocationDto> result, int rankNum, long studentId) {
        Set<String> difficultRankWordSet = CorpusConstant.RANK_NUM_TO_DIFFICULT_WORD_SET.get(rankNum);
        Set<String> rankWordSet = CorpusConstant.RANK_NUM_TO_WORD_SET.get(rankNum);
        Set<String> studentRankWordSet = studentRankWordService.getStudentRankWord(studentId, rankNum);
        for (int i = 0; i < result.size(); i++) {
            CollocationDto coll = result.get(i);
            String firstWord = coll.getFirstWord();
            String secondWord = coll.getSecondWord();
            String thirdWord = coll.getThirdWord();
            if (rankWordSet.contains(firstWord) && !studentRankWordSet.contains(firstWord)) {
                firstWord = CorpusConstant.RANK_WORD_STRENGTHEN_OPEN_LABEL + firstWord + CorpusConstant.RANK_WORD_STRENGTHEN_CLOSE_LABEL;
            } else if (difficultRankWordSet.contains(firstWord) && !difficultRankWordSet.contains(firstWord)) {
                firstWord = CorpusConstant.DIFFICULT_WORD_STRENGTHEN_OPEN_LABEL + firstWord + CorpusConstant.DIFFICULT_WORD_STRENGTHEN_CLOSE_LABEL;
            }

            if (rankWordSet.contains(secondWord) && !studentRankWordSet.contains(secondWord)) {
                secondWord = CorpusConstant.RANK_WORD_STRENGTHEN_OPEN_LABEL + secondWord + CorpusConstant.RANK_WORD_STRENGTHEN_CLOSE_LABEL;
            } else if (difficultRankWordSet.contains(secondWord) && !difficultRankWordSet.contains(secondWord)) {
                secondWord = CorpusConstant.DIFFICULT_WORD_STRENGTHEN_OPEN_LABEL + secondWord + CorpusConstant.DIFFICULT_WORD_STRENGTHEN_CLOSE_LABEL;
            }

            if (rankWordSet.contains(thirdWord) && !studentRankWordSet.contains(thirdWord)) {
                thirdWord = CorpusConstant.RANK_WORD_STRENGTHEN_OPEN_LABEL + thirdWord + CorpusConstant.RANK_WORD_STRENGTHEN_CLOSE_LABEL;
            } else if (difficultRankWordSet.contains(thirdWord) && !difficultRankWordSet.contains(thirdWord)) {
                thirdWord = CorpusConstant.DIFFICULT_WORD_STRENGTHEN_OPEN_LABEL + thirdWord + CorpusConstant.DIFFICULT_WORD_STRENGTHEN_CLOSE_LABEL;
            }

            coll.setFirstWord(firstWord);
            coll.setSecondWord(secondWord);
            coll.setThirdWord(thirdWord);
            result.set(i, coll);
        }
    }
}
