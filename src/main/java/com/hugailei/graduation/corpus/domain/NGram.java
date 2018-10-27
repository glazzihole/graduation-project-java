package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HU Gailei
 * @date 2018/10/27
 * <p>
 * description: ngram信息
 * </p>
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NGram {

    private int id;

    /**
     * ngram 单词串
     */
    private String ngramStr;

    /**
     * n值
     */
    private int nValue;

    /**
     * ngram出现的频次
     */
    private int freq;
}
