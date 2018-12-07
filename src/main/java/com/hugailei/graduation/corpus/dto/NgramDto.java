package com.hugailei.graduation.corpus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/12/7
 * <p>
 * description: ngram相关
 * </p>
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NgramDto implements Serializable {

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
