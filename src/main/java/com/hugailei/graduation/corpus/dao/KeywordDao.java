package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/9
 * <p>
 * description:
 * </p>
 **/
public interface KeywordDao extends JpaRepository<Keyword, Long> {

    /**
     * 按照指定语料库和参考语料库进行查询
     *
     * @param corpus
     * @param refCorpus
     * @return
     */
    List<Keyword> findByCorpusAndRefCorpusOrderByKeynessDesc(String corpus, String refCorpus);
}
