package com.hugailei.graduation.corpus.service.impl;

import com.bfsuolframework.core.utils.StringUtils;
import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dao.SentenceDao;
import com.hugailei.graduation.corpus.domain.Sentence;
import com.hugailei.graduation.corpus.domain.SentencePattern;
import com.hugailei.graduation.corpus.dto.SentenceDto;
import com.hugailei.graduation.corpus.elasticsearch.SentenceElasticSearch;
import com.hugailei.graduation.corpus.service.SentenceService;
import com.hugailei.graduation.corpus.service.StudentRankWordService;
import com.hugailei.graduation.corpus.util.SentenceAnalysisUtil;
import com.hugailei.graduation.corpus.util.SentenceRankUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description:
 * </p>
 **/
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class SentenceServiceImpl implements SentenceService  {

    @Autowired
    private SentenceElasticSearch sentenceElasticSearch;

    @Autowired
    private SentenceDao sentenceDao;

    @Autowired
    private StudentRankWordService studentRankWordService;

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<SentenceDto> sentenceElasticSearch(String keyword, String corpus) {
        try {
            log.info("sentenceElasticSearch | keyword: {}, corpus: {}", keyword, corpus);
            // 查询条件
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchPhraseQuery("sentence", keyword))
                    .must(QueryBuilders.termQuery("corpus", corpus));

            List<SentenceDto> resultList = new ArrayList<>();
            sentenceElasticSearch.search(queryBuilder).forEach(s -> {
                SentenceDto sentenceDto = new SentenceDto();
                sentenceDto.setId(s.getId());
                sentenceDto.setSentence(s.getSentence());
                resultList.add(sentenceDto);
            });

            return resultList;
        } catch (Exception e) {
            log.error("sentenceElasticSearch | error: {}", e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    @Cacheable(value = "corpus", key = "#sentenceIdList.toString()", unless = "#result eq null")
    public List<String> searchSentenceById(List<Long> sentenceIdList,
                                           Integer topic,
                                           Integer rankNum,
                                           HttpServletRequest request) {
        try {
            log.info("searchSentenceById | sentence id list: {}, rank num: {}", sentenceIdList.toString(), rankNum);
            List<String> sentenceStringList = new ArrayList<>();
            List<Sentence> sentenceList = sentenceDao.findAllById(sentenceIdList);
            for (Sentence sentence : sentenceList) {
                if (topic == null) {
                    sentenceStringList.add(sentence.getSentence());
                } else {
                    if (topic.intValue() == sentence.getTopic()) {
                        sentenceStringList.add(sentence.getSentence());
                    }
                }
            }

            List<String> resultList = SentenceRankUtil.orderSentenceByRankNumAsc(sentenceStringList);
            if (rankNum != null) {
                long studentId = (long)request.getSession().getAttribute("student_id");
                labelWord(resultList, rankNum, studentId);
            }
            return resultList;
        } catch (Exception e) {
            log.error("searchSentenceById | error: {}", e);
           return null;
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    @Cacheable(
            value = "corpus",
            key = "#keyword + '_' + #sentenceIdList.toString() + '_' + #corpus",
            unless = "#result eq null"
    )
    public List<SentenceDto> filterSentence(String keyword, List<Long> sentenceIdList, String corpus) {
        try {
            log.info("filterSentence | keyword: {}, sentence size: {}, corpus: {}", keyword, sentenceIdList.size(), corpus);
            List<Long> elasticSearchIdList = sentenceElasticSearch(keyword, corpus)
                    .stream()
                    .map(sentenceDto -> sentenceDto.getId())
                    .collect(Collectors.toList());
            Set<Long> elasticSearchSentenceIdSet = new LinkedHashSet<>(elasticSearchIdList);
            Set<Long> sentenceIdSet = new LinkedHashSet<>(sentenceIdList);
            // 求两个集合中的并集
            elasticSearchSentenceIdSet.retainAll(sentenceIdSet);
            List<Long> resultSentenceIdList = new ArrayList<>(elasticSearchSentenceIdSet);
            log.info("filterSentence | sentence size after filter: {}", resultSentenceIdList.size());
            List<SentenceDto> resultList = sentenceDao.findAllById(resultSentenceIdList).stream().map(sentence -> {
                SentenceDto sentenceDto = new SentenceDto();
                BeanUtils.copyProperties(sentence, sentenceDto);
                return sentenceDto;
            }).collect(Collectors.toList());
            return resultList;
        } catch (Exception e) {
            log.error("filterSentence | error: {}", e);
            return null;
        }
    }

    @Override
    public boolean updateSentenceElasticSearch(String corpus) {
        try {
            log.info("updateSentenceElasticSearch | corpus: {}", corpus);
            List<Sentence> sentenceList;
            if (StringUtils.isBlank(corpus)) {
                sentenceList = sentenceDao.findAll().stream().map(s -> {
                    Sentence sentence = new Sentence();
                    sentence.setCorpus(s.getCorpus());
                    sentence.setSentence(s.getSentence());
                    sentence.setId(s.getId());
                    return sentence;
                }).collect(Collectors.toList());
            } else {
                sentenceList = sentenceDao.selectByCorpus(corpus);
            }
            if (!CollectionUtils.isEmpty(sentenceList)) {
                log.info("updateSentenceElasticSearch | update sentence es index start, sentence list size: {}", sentenceList.size());
                sentenceElasticSearch.saveAll(sentenceList);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<SentencePattern> getSentencePattern(String sentence) {
        try {
            log.info("getSentencePattern | sentence: {}", sentence);
            List<CoreMap> coreMapList = StanfordParserUtil.parse(sentence);
            List<SentencePattern> result = SentenceAnalysisUtil.findAllClauseType(coreMapList.get(0));
            return result;
        } catch (Exception e) {
            log.error("getSentencePattern | error: {}", e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<String> getSimpleSentence(String sentence) {
        try {
            log.info("getSimpleSentence | sentence: {}", sentence);
            List<CoreMap> coreMapList = StanfordParserUtil.parse(sentence);
            List<String> result = SentenceAnalysisUtil.getSimpleSentence(coreMapList.get(0));
            return result;
        } catch (Exception e) {
            log.error("getSimpleSentence | error: {}", e);
            return null;
        }
    }

    @Override
    public Integer getSentenceRankNum(String sentence) {
        try {
            log.info("getSentenceRankNum | sentence: {}", sentence);
            Integer result = SentenceRankUtil.sentenceRankNum(sentence);
            return result;
        } catch (Exception e) {
            log.error("getSentenceRankNum | error: {}", e);
            return null;
        }
    }

    /**
     * 对检索内容进行重构标注
     *
     * @param result
     * @param rankNum
     * @param studentId
     */
    private void labelWord(List<String> result, int rankNum, long studentId) {
        Set<String> difficultRankWordSet = CorpusConstant.RANK_NUM_TO_DIFFICULT_WORD_SET.get(rankNum);
        Set<String> rankWordSet = CorpusConstant.RANK_NUM_TO_WORD_SET.get(rankNum);
        Set<String> studentRankWordSet = studentRankWordService.getStudentRankWord(studentId, rankNum);
        for (int i = 0; i < result.size(); i++) {
            String sentence = result.get(i);
            String labeledSentence = "";
            for (String word : sentence.split(" ")) {
                if (rankWordSet.contains(word) && !studentRankWordSet.contains(word)) {
                    word = CorpusConstant.RANK_WORD_STRENGTHEN_OPEN_LABEL + word + CorpusConstant.RANK_WORD_STRENGTHEN_CLOSE_LABEL;
                } else if (difficultRankWordSet.contains(word) && !studentRankWordSet.contains(word)) {
                    word = CorpusConstant.DIFFICULT_WORD_STRENGTHEN_OPEN_LABEL + word + CorpusConstant.DIFFICULT_WORD_STRENGTHEN_CLOSE_LABEL;
                }
                labeledSentence = labeledSentence + " " + word;
            }
            result.set(i, labeledSentence);
        }
    }
}
