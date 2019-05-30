package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dao.RankWordDao;
import com.hugailei.graduation.corpus.domain.RankWord;
import com.hugailei.graduation.corpus.service.RankWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HU Gailei
 * @date 2019/1/10
 * <p>
 * description:
 * </p>
 **/
@Service
@Slf4j
public class RankWordServiceImpl implements RankWordService {

    @Autowired
    private RankWordDao rankWordDao;

    @Override
    public Set<String> findMoreDifficultRankWord(int rankNum) {
        try {
            log.info("findMoreDifficultRankWord | rank num: {}", rankNum);
            List<String> rankWordList = rankWordDao.findAllByRankNumIsGreaterThanEqual(rankNum)
                    .stream()
                    .map(e -> {
                        return e.getWord();
                    })
                    .collect(Collectors.toList());
            Set<java.lang.String> rankWordSet = new HashSet<>(rankWordList);
            return rankWordSet;
        } catch (Exception e) {
            log.error("findMoreDifficultRankWord | error: {}", e);
            return null;
        }
    }

    @Override
    public List<String> findWordByRankNum(int rankNum) {
        try {
            log.info("findWordByRankNum | rank num: {}", rankNum);
            RankWord rankWord = new RankWord();
            rankWord.setRankNum(rankNum);
            Example<RankWord> rankWordExample = Example.of(rankWord);
            List<String> rankWordList = rankWordDao.findAll(rankWordExample)
                    .stream()
                    .map(e -> {
                        return e.getWord();
                    })
                    .collect(Collectors.toList());
            return rankWordList;
        } catch (Exception e) {
            log.error("findWordByRankNum | error: {}", e);
            return null;
        }
    }
}
