package com.hugailei.graduation.corpus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HU Gailei
 * @date 2018/10/11
 * <p>
 * description:
 * <p/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordDto {
    private Long id;

    private String word;

    private String pos;

    private String lemma;

    private Integer freq;

    private String sentenceIds;

    private String corpus;
}
