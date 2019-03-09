package com.hugailei.graduation.corpus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("ngram_string")
    private String ngramStr;

    /**
     * n值
     */
    @JsonProperty("n_value")
    private int nValue;

    /**
     * ngram出现的频次
     */
    private int freq;
}
