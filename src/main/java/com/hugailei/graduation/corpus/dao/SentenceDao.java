package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/6
 * <p>
 * description:
 * <p/>
 */
@Repository
public interface SentenceDao extends JpaRepository<Sentence, Long> {
    /**
     * 按语料库查找，只获取id与sentence字段
     *
     * @param corpus
     * @return
     */
    @Query("select id, sentence, corpus from Sentence where corpus = ?1")
    List<Sentence> selectByCorpus(String corpus);
}
