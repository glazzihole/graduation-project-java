package com.hugailei.graduation.corpus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("text_id")
    private Long textId;

    @JsonProperty("word_count")
    private Integer wordCount;

    /**
     * 命中词左边的句子
     */
    @JsonProperty("hit_left")
    private String hitLeft;

    /**
     * 命中词右边的句子
     */
    @JsonProperty("hit_right")
    private String hitRight;

    /**
     * 命中词
     */
    private String hit;
}
