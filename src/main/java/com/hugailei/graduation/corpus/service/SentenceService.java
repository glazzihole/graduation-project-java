package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.domain.SentencePattern;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description: 查询语料库中的句子
 * </p>
 **/
public interface SentenceService {

    /**
     * 按照句子ID进行查询
     *
     * @param sentenceIdList
     * @param topic
     * @param rankNum
     * @param request
     * @return
     */
    List<String> searchSentenceById(List<Long> sentenceIdList,
                                    Integer topic,
                                    Integer rankNum,
                                    HttpServletRequest request);

    /**
     * 获取句子的所有句型信息
     *
     * @param sentence
     * @return
     */
    List<SentencePattern> getSentencePattern(String sentence);

    /**
     * 获取句子中的简单句
     *
     * @param sentence
     * @return
     */
    List<String> getSimpleSentence(String sentence);

    /**
     * 获取句子的等级
     *
     * @param sentence
     * @return
     */
    Integer getSentenceRankNum(String sentence);
}
