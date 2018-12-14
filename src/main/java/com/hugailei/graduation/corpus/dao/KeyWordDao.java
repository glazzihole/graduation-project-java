package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.KeyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/9
 * <p>
 * description:
 * </p>
 **/
public interface KeyWordDao extends JpaRepository<KeyWord, Long> {

    /**
     * 按照指定语料库和参考语料库进行查询
     *
     * @param corpus
     * @param refCorpus
     * @return
     */
    List<KeyWord> findByCorpusAndRefCorpusOrderByKeynessDesc(String corpus, String refCorpus);
}
