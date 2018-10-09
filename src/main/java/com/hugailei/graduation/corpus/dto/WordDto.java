package com.hugailei.graduation.corpus.dto;

import lombok.Data;

/**
 * @author HU Gailei
 * @date 2018/10/11
 * <p>
 * description:
 * <p/>
 */
@Data
public class WordDto {
    private Long id;

    private String word;

    private String pos;

    private String lemma;

    private Integer freq;

    private String sentenceIds;

    private String corpus;
}
