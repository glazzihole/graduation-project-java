package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.dto.CollocationDto;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description: 搭配查询
 * </p>
 **/
public interface CollocationService {
    /**
     * 查询单词的搭配
     *
     * @param collocationDto
     * @return
     */
    List<CollocationDto> searchCollocationOfWord(CollocationDto collocationDto);

    /**
     * 查询同义搭配
     *
     * @param collocationDto
     * @return
     */
    List<CollocationDto> searchSynonymousCollocation(CollocationDto collocationDto);
}
