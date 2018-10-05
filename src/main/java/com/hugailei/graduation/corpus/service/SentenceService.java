package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.dto.DependencyDto;
import com.hugailei.graduation.corpus.dto.SentenceDto;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description:
 * </p>
 **/
public interface SentenceService {
    /**
     * 从数据库中查询句子
     *
     * @return
     */
    List<SentenceDto> searchSentenceFromDataBase();

    /**
     * 获取一个文本中的所有依存关系
     *
     * @param text
     * @return
     */
    List<DependencyDto> getDependency(String text);
}
