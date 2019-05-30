package com.hugailei.graduation.corpus.service.impl;

import com.bfsuolframework.core.utils.StringUtils;
import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dao.SentenceDao;
import com.hugailei.graduation.corpus.domain.Sentence;
import com.hugailei.graduation.corpus.domain.SentencePattern;
import com.hugailei.graduation.corpus.service.SentenceService;
import com.hugailei.graduation.corpus.service.StudentRankWordService;
import com.hugailei.graduation.corpus.util.SentenceAnalysisUtil;
import com.hugailei.graduation.corpus.util.SentenceRankUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private SentenceDao sentenceDao;

    @Autowired
    private StudentRankWordService studentRankWordService;

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
    @Cacheable(value = "corpus", key = "'sentence_pattern_'+#sentence", unless = "#result eq null")
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
    @Cacheable(value = "corpus", key = "'simple_sentence'+#sentence", unless = "#result eq null")
//    @HystrixCommand(
//            fallbackMethod = "getSimpleSentenceFallBack",
//            commandProperties = {
//                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value ="5000"),
//            }
//    )
    public List<String> getSimpleSentence(String sentence) {
        try {
            log.info("getSimpleSentence | sentence: {}", sentence);
            if (StringUtils.isBlank(sentence)) {
                return null;
            }
            List<CoreMap> coreMapList = StanfordParserUtil.simpleParse(sentence);
//            List<String> result = SentenceAnalysisUtil.getSimpleSentence(coreMapList.get(0));
            List<String> result = SentenceAnalysisUtil.getSimpleSentenceOnlyByParser(coreMapList.get(0));
            return result;
        } catch (Exception e) {
            log.error("getSimpleSentence | error: {}", e);
            return null;
        }
    }

    public List<String> getSimpleSentenceFallBack(String sentence) {
        try {
            log.info("getSimpleSentenceFallBack | sentence: {}", sentence);
            if (StringUtils.isBlank(sentence)) {
                return null;
            }
            List<CoreMap> coreMapList = StanfordParserUtil.simpleParse(sentence);
            List<String> result = SentenceAnalysisUtil.getSimpleSentenceOnlyByParser(coreMapList.get(0));
            return result;
        } catch (Exception e) {
            log.error("getSimpleSentenceFallBack | error: {}", e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    @Cacheable(value = "corpus", key = "'sentence_rank_num'+#sentence", unless = "#result eq null")
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
