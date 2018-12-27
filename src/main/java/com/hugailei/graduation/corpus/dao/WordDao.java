package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author HU Gailei
 * @date 2018/10/11
 * <p>
 * description:
 * <p/>
 */
@Repository
public interface WordDao extends JpaRepository<Word, Long>{

    /**
     * 查询指定语料库中的单词列表，并按照频次降序排序
     *
     * @param corpus
     * @return
     */
    List<Word> findAllByCorpusOrderByFreqDesc(String corpus);
}
