package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Collocation;
import com.hugailei.graduation.corpus.domain.CollocationWithTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/25
 * <p>
 * description:
 * </p>
 **/
@Repository
public interface CollocationWithTopicDao extends JpaRepository<CollocationWithTopic, Long> {
    /**
     * 根据搭配词串和语料库查询所有
     *
     * @param wordPair
     * @param corpus
     * @return
     */
    @Query("SELECT topic, sum(freq) as freq " +
            "FROM tb_collocation_with_topic " +
            "WHERE word_pair = ?1 AND corpus = ?2 " +
            "GROUP BY topic")
    List<CollocationWithTopic> findByWordPairAndCorpus(String wordPair, String corpus);
}
