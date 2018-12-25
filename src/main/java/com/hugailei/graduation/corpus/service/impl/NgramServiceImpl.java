package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dao.NgramDao;
import com.hugailei.graduation.corpus.dao.NgramWithTopicDao;
import com.hugailei.graduation.corpus.dto.NgramDto;
import com.hugailei.graduation.corpus.service.NgramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public List<NgramDto> ngramList(String corpus, int nValue, int topic) {
        try {
            log.info("ngramList | corpus: {}, nValue: {}", corpus, nValue);
            List<NgramDto> ngramDtoList;
            // 不带主题查询
            if (topic == 0) {
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
            return ngramDtoList;
        } catch (Exception e) {
            log.error("ngramList | error: {}", e);
            return null;
        }
    }
}
