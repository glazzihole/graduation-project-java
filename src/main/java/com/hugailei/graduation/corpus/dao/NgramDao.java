package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Ngram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/7
 * <p>
 * description:
 * </p>
 **/
@Repository
public interface NgramDao extends JpaRepository<Ngram, Long> {
    /**
     * 按语料库名称和n值进行查询
     *
     * @param corpus
     * @param nValue
     * @return
     */
    List<Ngram> findByCorpusAndNValue(String corpus, int nValue);
}
