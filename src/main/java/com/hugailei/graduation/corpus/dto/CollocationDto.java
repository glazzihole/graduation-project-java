package com.hugailei.graduation.corpus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
 * description: 搭配信息
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollocationDto implements Serializable {

    private Long id;

    @JsonProperty("first_word")
    private String firstWord;

    @JsonProperty("first_pos")
    private String firstPos;

    @JsonProperty("second_word")
    private String secondWord;

    @JsonProperty("second_pos")
    private String secondPos;

    @JsonProperty("third_pos")
    private String thirdPos;

    @JsonProperty("third_word")
    private String thirdWord;

    @JsonProperty("sentence_ids")
    private String sentenceIds;

    private Integer freq;

    @NotNull
    private String corpus;

    /**
     * 用于标识需要查找的单词在搭配中的位序 1为第一个单词，2为第二个单词. 3为第三个单词
     */
    private Integer position;

    /**
     * 主题
     */
    private Integer topic;

    /**
     * 等级
     */
    private Integer rankNum;


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
        @JsonProperty("word_collocation_list")
        private List wordCollocationList;


        /**
         * 词性搭配列表
         */
        @JsonProperty("pos_collocation_list")
        private List posCollocationList;
    }


    /**
     * @author HU Gailei
     * @date 2019/1/29
     * <p>
     * description: 搭配词典中的信息
     * </p>
     **/
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CollocationDictInfo {

        /**
         * 单词词性
         */
        @JsonProperty("word_pos")
        private String wordPos;

        /**
         * 该单词搭配词的信息
         */
        @JsonProperty("collocation_word_info_list")
        private List collocationWordInfoList;

    }

    /**
     * @author HU Gailei
     * @date 2019/1/29
     * <p>
     * description: 搭配词典中搭配词的信息
     * </p>
     **/
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CollocationWordInfo {
        /**
         * 搭配词词性
         */
        @JsonProperty("collocation_pos")
        private String collocationPos;

        /**
         * 搭配
         */
        @JsonProperty("collocation_list")
        private List<String> collocationList;

    }
}
