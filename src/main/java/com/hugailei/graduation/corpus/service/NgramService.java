package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.dto.NgramDto;

import javax.servlet.http.HttpServletRequest;
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
     * 获取指定语料库中n为指定值(且为指定主题)的ngram
     *
     * @param corpus
     * @param nValue
     * @param topic
     * @param rankNum
     * @param request
     * @return
     */
    List<NgramDto> ngramList(String corpus, int nValue, Integer topic, Integer rankNum, HttpServletRequest request);
}
