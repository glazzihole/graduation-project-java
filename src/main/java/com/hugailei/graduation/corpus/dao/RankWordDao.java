package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.RankWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2019/1/9
 * <p>
 * description:
 * </p>
 **/
@Repository
public interface RankWordDao extends JpaRepository<RankWord, Long> {
    /**
     * 查询大于等于指定等级的词汇
     *
     * @param rankNum
     * @return
     */
    List<RankWord> findAllByRankNumIsGreaterThanEqual(int rankNum);
}
