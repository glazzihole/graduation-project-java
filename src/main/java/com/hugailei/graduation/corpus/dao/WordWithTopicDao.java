package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.WordWithTopic;
import org.springframework.data.jpa.repository.JpaRepository;
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
public interface WordWithTopicDao extends JpaRepository<WordWithTopic, Long> {
    /**
     * 查询指定语料库中指定主题下的单词列表，并按照频次降序排序
     *
     * @param corpus    语料库名称
     * @param topic     主题标号
     * @return
     */
    List<WordWithTopic> findAllByCorpusAndTopicOrderByFreqDesc(String corpus, int topic);
}
