package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.dto.WordDto;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/11
 * <p>
 * description:
 * <p/>
 */
public interface WordService {

    /**
     * 单词——频率列表查询
     *
     * @param corpus    语料库名称
     * @param topic     主题标号
     * @param rankNum   难度等级
     * @return
     */
    List<WordDto> wordList(String corpus, int topic, Integer rankNum);

    /**
     * 单词的词性、原型及形态查询
     *
     * @param query     待匹配的词汇
     * @param corpus    语料库名称
     * @param searchType 搜索类型 lemma——查询所有原型；pos——查询所有词性；form——查询所有形态
     * @return
     */
    List<WordDto> searchAll(String query, String corpus, String searchType);

    /**
     * 查看单词详情，查询单词所有形态、词性、原型，及相关频率信息
     *
     * @param query     关键词
     * @param corpus    语料库
     * @param queryType 关键词的类型：lemma——原型，form——单词（词形）
     * @return
     */
    List<WordDto> searchDetail(String query, String corpus, String queryType);

    /**
     * 查询所输入内容在不同语料库中的分布
     *
     * @param query         关键词
     * @param queryType     关键词的类型
     * @return
     */
    List<WordDto> corpusDistribution(String query, String queryType);

    /**
     * 查询所输入内容在不同主题中的分布
     *
     * @param query     关键词
     * @param queryType 关键词的类型
     * @param corpus    语料库
     * @return
     */
    List<WordDto> topicDistribution(String query, String queryType, String corpus);
}
