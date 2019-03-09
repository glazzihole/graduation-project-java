package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dao.KeywordDao;
import com.hugailei.graduation.corpus.dto.WordDto;
import com.hugailei.graduation.corpus.service.KeyWordService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
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
@Transactional(rollbackFor = Exception.class)
public class KeyWordServiceImpl implements KeyWordService {

    @Autowired
    private KeywordDao keyWordDao;

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    @Cacheable(value = "corpus", key = "#corpus + '_' + #refCorpus + '_' + #rankNum", unless = "#result eq null")
    public List<WordDto> keyWordList(String corpus, String refCorpus, Integer rankNum) {
        try {
            log.info("keyWordList | corpus: {}, refCorpus: {}, rank num: {}", corpus, refCorpus, rankNum);
            List<WordDto> wordDtoList = keyWordDao.findByCorpusAndRefCorpusOrderByKeynessDesc(corpus, refCorpus)
                    .stream()
                    .map(w -> {
                        WordDto wordDto = new WordDto();
                        BeanUtils.copyProperties(w, wordDto);
                        return wordDto;
                    })
                    .collect(Collectors.toList());
            if (rankNum != null) {
                Set<String> rankWordSet = CorpusConstant.RANK_NUM_TO_WORD_SET.get(rankNum);
                for (int i = 0; i < wordDtoList.size(); i++) {
                    WordDto wordDto = wordDtoList.get(i);
                    String word = wordDto.getForm();
                    if (rankWordSet.contains(word)) {
                        word = CorpusConstant.STRENGTHEN_OPEN_LABEL + word + CorpusConstant.STRENGTHEN_CLOSE_LABEL;
                    }
                    wordDto.setForm(word);
                    wordDtoList.set(i, wordDto);
                }
            }
            return wordDtoList;
        } catch (Exception e) {
            log.error("keyWordList | error: {}", e);
            return null;
        }
    }
}
