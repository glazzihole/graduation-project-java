package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.dto.NgramDto;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/7
 * <p>
 * description:
 * </p>
 **/
public interface NgramService {

    /**
     * 获取指定语料库中n为指定值的ngrams
     *
     * @param corpus
     * @param nValue
     * @return
     */
    List<NgramDto> ngramList(String corpus, int nValue);
}
