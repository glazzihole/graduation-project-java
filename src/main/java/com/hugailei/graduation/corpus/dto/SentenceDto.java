package com.hugailei.graduation.corpus.dto;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description:
 * </p>
 **/
public class SentenceDto {
    private Long id;
    private String setence;
    private Long textId;
    private Integer wordCount;

    /**
     * 命中词左边的句子
     */
    private String hitleft;

    /**
     * 命中词右边的句子
     */
    private String hitRight;

    /**
     * 命中词
     */
    private String hit;
}
