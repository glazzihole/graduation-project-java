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
public class WordDto {
    private Long id;

    private String form;

    private String pos;

    private String lemma;

    private Integer freq;

    private String sentenceIds;

    private String corpus;

    private String refCorpus;

    /**
     * keyword中的关键性值
     */
    private Double keyness;

    /**
     * 主题
     */
    private Integer topic;

    public WordDto() {

    }

    public WordDto(Long id, String form, String pos, String lemma, Integer freq, String corpus) {
        this.id = id;
        this.form = form;
        this.pos = pos;
        this.lemma = lemma;
        this.freq = freq;
        this.corpus = corpus;
    }
}
