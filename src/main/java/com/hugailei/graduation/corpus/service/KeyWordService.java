package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.dto.WordDto;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/8
 * <p>
 * description:
 * </p>
 **/
public interface KeyWordService {
    /**
     * 查询指定语料库相对于参考语料库的关键词
     * @param corpus    语料库
     * @param refCorpus 参考语料库
     * @param rankNum   等级
     * @return
     */
    List<WordDto> keyWordList(String corpus, String refCorpus, Integer rankNum);
}
