package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dao.WordDao;
import com.hugailei.graduation.corpus.domain.QWord;
import com.hugailei.graduation.corpus.dto.WordDto;
import com.hugailei.graduation.corpus.service.WordService;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HU Gailei
 * @date 2018/10/11
 * <p>
 * description:
 * <p/>
 */
@Service
@Slf4j
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class WordServiceImpl implements WordService {

    @Autowired
    private WordDao wordDao;

    @Override
    public List<WordDto> searchAll(String query, String corpus, String searchType) {
        try {
            log.info("searchAll | query: {}, corpus: {}, searchType: {}", query, corpus, searchType);
            Predicate predicate;
            Map<String, WordDto> key2WordDto = new HashMap<>();
            switch (searchType) {
                case CorpusConstant.LEMMA:
                    predicate = QWord.word1.word.eq(query);
                    wordDao.findAll(predicate).forEach(word -> {
                        if (key2WordDto.containsKey(word.getLemma())) {
                            WordDto wordDto = key2WordDto.get(word.getLemma());
                            int freq = wordDto.getFreq() + word.getFreq();
                            wordDto.setFreq(freq);
                            key2WordDto.put(word.getLemma(), wordDto);
                        } else {
                            WordDto wordDto = new WordDto();
                            BeanUtils.copyProperties(word, wordDto);
                            key2WordDto.put(word.getLemma(), wordDto);
                        }
                    });
                    break;
                case CorpusConstant.POS:
                    predicate = QWord.word1.word.eq(query);
                    wordDao.findAll(predicate).forEach(word -> {
                        if (key2WordDto.containsKey(word.getPos())) {
                            WordDto wordDto = key2WordDto.get(word.getPos());
                            int freq = wordDto.getFreq() + word.getFreq();
                            wordDto.setFreq(freq);
                            key2WordDto.put(word.getPos(), wordDto);
                        } else {
                            WordDto wordDto = new WordDto();
                            BeanUtils.copyProperties(word, wordDto);
                            key2WordDto.put(word.getPos(), wordDto);
                        }
                    });
                    break;
                case CorpusConstant.FORM:
                    predicate = QWord.word1.lemma.eq(query);
                    wordDao.findAll(predicate).forEach(word -> {
                        if (key2WordDto.containsKey(word.getWord())) {
                            WordDto wordDto = key2WordDto.get(word.getWord());
                            int freq = wordDto.getFreq() + word.getFreq();
                            wordDto.setFreq(freq);
                            key2WordDto.put(word.getWord(), wordDto);
                        } else {
                            WordDto wordDto = new WordDto();
                            BeanUtils.copyProperties(word, wordDto);
                            key2WordDto.put(word.getWord(), wordDto);
                        }
                    });
                    break;
                default:
                    log.error("searchAll | wrong searchType");
                    return null;
            }
            List<WordDto> resultList = new ArrayList<>();
            resultList.addAll(key2WordDto.values());
            return resultList;
        } catch (Exception e) {
            log.error("searchAll | error: {}", e);
            return null;
        }
    }

    @Override
    public List<WordDto> searchDetail(String query, String corpus, String queryType) {
        try {
            log.info("searchDetail | query: {}, corpus: {}, queryType: {}", query, corpus, queryType);
            Predicate predicate;
            switch (queryType) {
                case CorpusConstant.LEMMA:
                    predicate = QWord.word1.lemma.eq(query);
                    break;
                case CorpusConstant.FORM:
                    predicate = QWord.word1.word.eq(query);
                    break;
                default:
                    log.error("searchDetail | wrong queryType");
                    return null;
            }
            List<WordDto> resultList = new ArrayList<>();
            wordDao.findAll(predicate).forEach(word -> {
                WordDto wordDto = new WordDto();
                BeanUtils.copyProperties(word, wordDto);
                resultList.add(wordDto);
            });
            return resultList;
        } catch (Exception e) {
            log.error("searchDetail | error: {}", e);
            return null;
        }
    }
}
