package com.hugailei.graduation.corpus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

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

    private String firstPos;

    private String secondWord;

    private String secondPos;

    private String thirdPos;

    private String thirdWord;

    private String sentenceIds;

    private Integer freq;

    @NotNull
    private String corpus;

    /**
     * 用于标识需要查找的单词在搭配中的位序 1为第一个单词，2为第二个单词. 3为第三个单词
     */
    private Integer position;

    private String type;


    /**
     * @author HU Gailei
     * @date 2018/11/18
     * <p>
     * description: 搭配组合情况
     * </p>
     **/
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CollocationInfo {
        /**
         * 单词元型搭配列表
         */
        private List wordCollocationList;


        /**
         * 词性搭配列表
         */
        private List posCollocationList;
    }
}
