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
     * @param content   待查询内容
     * @param corpus    语料库
     * @return
     */
    List<SentenceDto> sentenceElasticSearch(String content, String corpus);
}
