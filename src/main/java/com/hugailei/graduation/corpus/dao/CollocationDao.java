package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Collocation;
import com.hugailei.graduation.corpus.domain.CollocationWithTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description:
 * </p>
 **/
@Repository
public interface CollocationDao extends JpaRepository<Collocation, Long> {
    /**
     * 按照词对查询搭配，并按照频率降序排序
     *
     * @param wordPair
     * @return
     */
    List<Collocation> findAllByWordPairOrderByFreqDesc(String wordPair);

    @Query("SELECT corpus, sum(freq) as freq " +
            "FROM tb_collocation " +
            "WHERE word_pair = ?1 " +
            "GROUP BY corpus")
    List<CollocationWithTopic> findByWordPair(String wordPair);
}
