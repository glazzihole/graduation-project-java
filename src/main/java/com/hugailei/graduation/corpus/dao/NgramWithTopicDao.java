package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.NgramWithTopic;
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
public interface NgramWithTopicDao extends JpaRepository<NgramWithTopic, Long> {
    /**
     * 按语料库，ngram值及主题查找，并按频率降序排序
     *
     * @param courpus
     * @param nValue
     * @param topic
     */
    List<NgramWithTopic> findByCorpusAndNValueAndTopicOrderByFreqDesc(String courpus, int nValue, int topic);
}
