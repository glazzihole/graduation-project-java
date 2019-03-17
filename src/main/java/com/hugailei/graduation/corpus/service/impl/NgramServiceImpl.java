package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dao.NgramDao;
import com.hugailei.graduation.corpus.dao.NgramWithTopicDao;
import com.hugailei.graduation.corpus.dto.NgramDto;
import com.hugailei.graduation.corpus.service.NgramService;
import com.hugailei.graduation.corpus.service.StudentRankWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HU Gailei
 * @date 2018/12/7
 * <p>
 * description:
 * </p>
 **/
@Slf4j
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class NgramServiceImpl implements NgramService {

    @Autowired
    private NgramDao ngramDao;

    @Autowired
    private NgramWithTopicDao ngramWithTopicDao;

    @Autowired
    private StudentRankWordService studentRankWordService;

    @Override
    @Cacheable(
            value = "corpus",
            key = "#corpus + '_' + #nValue + '_' + #topic + '_' + #rankNum",
            unless = "#result eq null"
    )
    public List<NgramDto> ngramList(String corpus,
                                    int nValue,
                                    Integer topic,
                                    Integer rankNum,
                                    HttpServletRequest request) {
        try {
            log.info("ngramList | corpus: {}, nValue: {}, rank num: {}", corpus, nValue, rankNum);
            List<NgramDto> ngramDtoList;
            // 不带主题查询
            if (topic == null) {
                ngramDtoList = ngramDao.findByCorpusAndNValueOrderByFreqDesc(corpus, nValue)
                        .stream()
                        .map(n -> {
                            NgramDto ngramDto = new NgramDto();
                            BeanUtils.copyProperties(n, ngramDto);
                            return ngramDto;
                        })
                        .collect(Collectors.toList());
            }
            // 带主题查询
            else {
                ngramDtoList = ngramWithTopicDao.findByCorpusAndNValueAndTopicOrderByFreqDesc(corpus, nValue, topic)
                        .stream()
                        .map(n -> {
                            NgramDto ngramDto = new NgramDto();
                            BeanUtils.copyProperties(n, ngramDto);
                            return ngramDto;
                        })
                        .collect(Collectors.toList());
            }
            log.info("ngramList | ngram list size: {}", ngramDtoList.size());
            if (rankNum != null) {
                long studentId = (long)request.getSession().getAttribute("student_id");
                labelWord(ngramDtoList, rankNum, studentId);
            }
            return ngramDtoList;
        } catch (Exception e) {
            log.error("ngramList | error: {}", e);
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
    private void labelWord(List<NgramDto> result, int rankNum, long studentId) {
        Set<String> difficultRankWordSet = CorpusConstant.RANK_NUM_TO_DIFFICULT_WORD_SET.get(rankNum);
        Set<String> rankWordSet = CorpusConstant.RANK_NUM_TO_WORD_SET.get(rankNum);
        Set<String> studentRankWordSet = studentRankWordService.getStudentRankWord(studentId, rankNum);
        for (int i = 0; i < result.size(); i++) {
            NgramDto ngramDto = result.get(i);
            String ngramString = ngramDto.getNgramStr();
            String labeledNgramString = "";
            for (String word : ngramString.split(" ")) {
                if (rankWordSet.contains(word) && !studentRankWordSet.contains(word)) {
                    word = CorpusConstant.RANK_WORD_STRENGTHEN_OPEN_LABEL + word + CorpusConstant.RANK_WORD_STRENGTHEN_CLOSE_LABEL;
                } else if (difficultRankWordSet.contains(word) && !studentRankWordSet.contains(word)) {
                    word = CorpusConstant.DIFFICULT_WORD_STRENGTHEN_OPEN_LABEL + word + CorpusConstant.DIFFICULT_WORD_STRENGTHEN_CLOSE_LABEL;
                }
                labeledNgramString = ngramString + " " + word;
            }
            ngramDto.setNgramStr(labeledNgramString);
            result.set(i, ngramDto);
        }
    }
}
