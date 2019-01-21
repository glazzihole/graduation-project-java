package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.RankWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2019/1/9
 * <p>
 * description:
 * </p>
 **/
public interface RankWordDao extends JpaRepository<RankWord, Long> {
    /**
     * 查询大于等于指定等级的词汇
     *
     * @param rankNum
     * @return
     */
    List<RankWord> findAllByRankNumIsGreaterThanEqual(int rankNum);
}
