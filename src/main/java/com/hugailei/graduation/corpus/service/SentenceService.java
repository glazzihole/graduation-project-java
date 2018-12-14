package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.dto.SentenceDto;
import org.springframework.data.domain.Page;

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
     * @return
     */
    List<SentenceDto> searchSentenceById(List<Long> sentenceIdList);

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
}
