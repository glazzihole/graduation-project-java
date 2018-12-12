package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dao.KeyWordDao;
import com.hugailei.graduation.corpus.dto.WordDto;
import com.hugailei.graduation.corpus.service.KeyWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HU Gailei
 * @date 2018/12/9
 * <p>
 * description:
 * </p>
 **/
@Slf4j
@Service
public class KeyWordServiceImpl implements KeyWordService {

    @Autowired
    private KeyWordDao keyWordDao;

    @Override
    public List<WordDto> keyWordList(String corpus, String refCorpus) {
        try {
            log.info("keyWordList | corpus: {}, refCorpus: {}", corpus, refCorpus);
            List<WordDto> wordDtoList = keyWordDao.findByCorpusAndRefCorpusOrderByKeynessDesc(corpus, refCorpus).stream().map(w -> {
                WordDto wordDto = new WordDto();
                BeanUtils.copyProperties(w, wordDto);
                return wordDto;
            }).collect(Collectors.toList());

            return wordDtoList;
        } catch (Exception e) {
            log.error("keyWordList | error: {}", e);
            return null;
        }
    }
}