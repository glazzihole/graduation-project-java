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

    /**
     * 获取文章/句子中的搭配信息
     *
     * @param text
     * @return
     */
    CollocationDto.CollocationInfo getCollocationInText(String text);

    /**
     * 验证该词对是否为正确搭配，是返回true，否则返回false
     *
     * @param wordPair
     * @return
     */
    Boolean checkCollocation(String wordPair);

    /**
     * 验证多个词对是否为正确搭配，是则对应位置的list为true，否则返回false
     *
     * @param wordPair
     * @return
     */
    List<Boolean> checkCollocationList(String wordPair);

    /**
     * 推荐该搭配的同义搭配
     *
     * @param wordPair
     * @param posPair
     * @param rankNum
     * @return
     */
    List<CollocationDto> recommendSynonym(String wordPair, String posPair, Integer rankNum);

    /**
     * 查找单词在搭配词典中的搭配，按照搭配词词性及搭配分类
     *
     * @param word
     * @param rankNum
     * @return
     */
    List<CollocationDto.CollocationDictInfo> searchCollocationInDict(String word, Integer rankNum);
}
