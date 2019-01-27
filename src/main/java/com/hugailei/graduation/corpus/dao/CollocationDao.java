package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Collocation;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * 按照第一个单词，第二个单词，第三个单词的内容查询所有搭配，并按照频率降序排序
     *
     * @param firstWord
     * @param secondWord
     * @param thirdWord
     * @return
     */
    List<Collocation> findAllByFirstWordAndSecondWordAndThirdWordOrderByFreqDesc(String firstWord, String secondWord, String thirdWord);
}
