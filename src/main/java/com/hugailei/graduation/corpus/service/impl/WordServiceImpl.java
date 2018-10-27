package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dao.WordDao;
import com.hugailei.graduation.corpus.domain.Word;
import com.hugailei.graduation.corpus.dto.WordDto;
import com.hugailei.graduation.corpus.service.WordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
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

            Map<String, Integer> key2Freq = new HashMap<>();
            Word word = new Word();
            word.setCorpus(corpus);
            List<WordDto> resultList = new ArrayList<>();
            switch (searchType) {
                case CorpusConstant.LEMMA:
                    word.setWord(query);
                    Example<Word> example = Example.of(word);
                    wordDao.findAll(example).forEach(w -> {
                        if (key2Freq.containsKey(w.getLemma())) {
                            int freq = key2Freq.get(w.getLemma()) + w.getFreq();
                            key2Freq.put(w.getLemma(), freq);
                        } else {
                            key2Freq.put(w.getLemma(), w.getFreq());
                        }
                    });
                    long index = 1L;
                    for(Map.Entry<String, Integer> entry : key2Freq.entrySet()){
                        resultList.add(
                                new WordDto(index++, query, null, entry.getKey(), entry.getValue(),null, corpus)
                        );
                    }
                    break;
                case CorpusConstant.POS:
                    word.setWord(query);
                    example = Example.of(word);
                    wordDao.findAll(example).forEach(w -> {
                        if (key2Freq.containsKey(w.getPos())) {
                            int freq = key2Freq.get(w.getPos()) + w.getFreq();
                            key2Freq.put(w.getPos(), freq);
                        } else {
                            key2Freq.put(w.getPos(), w.getFreq());
                        }
                    });
                    index = 1L;
                    for(Map.Entry<String, Integer> entry : key2Freq.entrySet()){
                        resultList.add(
                                new WordDto(index++, query, entry.getKey(), null, entry.getValue(),null, corpus)
                        );
                    }
                    break;
                case CorpusConstant.FORM:
                    word.setLemma(query);
                    example = Example.of(word);
                    wordDao.findAll(example).forEach(w -> {
                        if (key2Freq.containsKey(w.getWord())) {
                            int freq = key2Freq.get(w.getWord()) + w.getFreq();
                            key2Freq.put(w.getWord(), freq);
                        } else {
                            key2Freq.put(w.getWord(), w.getFreq());
                        }
                    });
                    index = 1L;
                    for(Map.Entry<String, Integer> entry : key2Freq.entrySet()){
                        resultList.add(
                                new WordDto(index++, entry.getKey(), null, query, entry.getValue(),null, corpus)
                        );
                    }
                    break;
                default:
                    log.error("searchAll | wrong searchType");
                    return null;
            }

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
            Word word = new Word();
            word.setCorpus(corpus);
            Example<Word> example;
            switch (queryType) {
                case CorpusConstant.LEMMA:
                    word.setLemma(query);
                    break;
                case CorpusConstant.FORM:
                    word.setWord(query);
                    break;
                default:
                    log.error("searchDetail | wrong queryType");
                    return null;
            }
            List<WordDto> resultList = new ArrayList<>();
            example = Example.of(word);
            wordDao.findAll(example).forEach(w -> {
                WordDto wordDto = new WordDto();
                BeanUtils.copyProperties(w, wordDto);
                resultList.add(wordDto);
            });
            return resultList;
        } catch (Exception e) {
            log.error("searchDetail | error: {}", e);
            return null;
        }
    }

    @Override
    public List<WordDto> searchCorpus(String query, String queryType) {
        try {
            log.info("searchCorpus | query: {}, queryType: {}", query, queryType);
            Word word = new Word();
            Example<Word> example;
            final Map<String, WordDto> corpus2WordDto = new HashMap<>();
            List<WordDto> resultList = new ArrayList<>();
            switch (queryType) {
                case CorpusConstant.LEMMA:
                    word.setLemma(query);
                    example = Example.of(word);
                    wordDao.findAll(example).forEach(w -> {
                        WordDto wordDto = new WordDto();
                        wordDto.setLemma(query);
                        wordDto.setCorpus(w.getCorpus());
                        wordDto.setFreq(w.getFreq());
                        if(corpus2WordDto.containsKey(wordDto.getCorpus())) {
                            int freq = corpus2WordDto.get(wordDto.getCorpus()).getFreq() + wordDto.getFreq();
                            wordDto.setFreq(freq);
                        } else {
                            corpus2WordDto.put(wordDto.getCorpus(), wordDto);
                        }
                    });
                    break;
                case CorpusConstant.FORM:
                    word.setWord(query);
                    example = Example.of(word);
                    wordDao.findAll(example).forEach(w -> {
                        WordDto wordDto = new WordDto();
                        wordDto.setWord(query);
                        wordDto.setCorpus(w.getCorpus());
                        wordDto.setFreq(w.getFreq());
                        if(corpus2WordDto.containsKey(wordDto.getCorpus())) {
                            int freq = corpus2WordDto.get(wordDto.getCorpus()).getFreq() + wordDto.getFreq();
                            wordDto.setFreq(freq);
                        } else {
                            corpus2WordDto.put(wordDto.getCorpus(), wordDto);
                        }
                    });
                    break;
                case CorpusConstant.POS:
                    word.setPos(query);
                    example = Example.of(word);
                    wordDao.findAll(example).forEach(w -> {
                        WordDto wordDto = new WordDto();
                        wordDto.setPos(query);
                        wordDto.setCorpus(w.getCorpus());
                        wordDto.setFreq(w.getFreq());
                        if(corpus2WordDto.containsKey(wordDto.getCorpus())) {
                            int freq = corpus2WordDto.get(wordDto.getCorpus()).getFreq() + wordDto.getFreq();
                            wordDto.setFreq(freq);
                        } else {
                            corpus2WordDto.put(wordDto.getCorpus(), wordDto);
                        }
                    });
                    break;
                default:
                    log.error("searchCorpus | wrong queryType");
                    return null;
            }
            resultList.addAll(corpus2WordDto.values());
            return resultList;
        } catch (Exception e) {
            log.error("searchCorpus | error: {}", e);
            return null;
        }
    }
}
