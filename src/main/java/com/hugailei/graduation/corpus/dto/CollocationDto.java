package com.hugailei.graduation.corpus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description:
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollocationDto implements Serializable {

    private Long id;

    private String firstWord;

    private String secondWord;

    private String firstPos;

    private String secondPos;

    private String sentenceIds;

    private int freq;

    @NotNull
    private String corpus;

    /**
     * 用于标识需要查找的单词在搭配中的位序 1为第一个单词，2为第二个单词
     */
    private int position;

    private String type;
}
