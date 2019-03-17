package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.domain.SentencePattern;
import com.hugailei.graduation.corpus.dto.SentenceDto;

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
     * es查询，查询数据库中指定语料库里包含与指定内容相关的例句
     *
     * @param keyword   待查询内容
     * @param corpus    语料库名称
     * @return
     */
    List<SentenceDto> sentenceElasticSearch(String keyword, String corpus);

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
     * 句子过滤，只保留包含指定关键字的例句
     *
     * @param keyword           关键字
     * @param sentenceIdList    句子ID列表
     * @param corpus            语料库名称
     * @return
     */
    List<SentenceDto> filterSentence(String keyword, List<Long> sentenceIdList, String corpus);

    /**
     * 更新es索引，更新成功返回true，否则返回false
     *
     * @param corpus
     * @return
     */
    boolean updateSentenceElasticSearch(String corpus);

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
}
