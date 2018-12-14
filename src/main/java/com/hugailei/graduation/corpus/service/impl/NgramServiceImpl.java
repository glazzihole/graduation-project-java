package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dao.NgramDao;
import com.hugailei.graduation.corpus.domain.Ngram;
import com.hugailei.graduation.corpus.dto.NgramDto;
import com.hugailei.graduation.corpus.service.NgramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
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
public class NgramServiceImpl implements NgramService {

    @Autowired
    private NgramDao ngramDao;

    @Override
    public List<NgramDto> ngramList(String corpus, int nValue) {
        try {
            log.info("ngramList | corpus: {}, nValue: {}", corpus, nValue);
            List<NgramDto> ngramList = ngramDao.findByCorpusAndNValueOrderByFreqDesc(corpus, nValue).stream().map(n -> {
                NgramDto ngramDto = new NgramDto();
                BeanUtils.copyProperties(n, ngramDto);
                return ngramDto;
            }).collect(Collectors.toList());

            return ngramList;
        } catch (Exception e) {
            log.error("ngramList | error: {}", e);
            return null;
        }
    }
}
