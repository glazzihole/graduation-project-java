package com.hugailei.graduation.corpus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description:
 * </p>
 **/
@Data
@NoArgsConstructor
public class SentenceDto implements Serializable {
    private Long id;
    private String sentence;
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
