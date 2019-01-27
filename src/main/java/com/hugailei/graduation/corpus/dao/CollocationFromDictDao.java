package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.CollocationFromDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2019/1/23
 * <p>
 * description:
 * </p>
 **/
@Repository
public interface CollocationFromDictDao extends JpaRepository<CollocationFromDict, Long> {
    /**
     * 在搭配词典数据中查找指定搭配，因只需验证是否存在，所以只要找到第一条即可
     *
     * @param collocation
     * @return
     */
    CollocationFromDict findFirstByCollocation(String collocation);

    /**
     * 查找指定单词的所有搭配信息列表
     *
     * @param word
     * @return
     */
    List<CollocationFromDict> findAllByWord(String word);
}
