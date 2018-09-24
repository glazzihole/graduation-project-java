package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/9/25
 * <p>
 * description: 单词信息
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_word")
public class Word implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "word")
    private String word;

    @Column(name = "pos")
    private String pos;

    @Column(name = "lemma")
    private String lemma;

    @Column(name = "freq")
    private Integer freq;

    @Column(name = "sentence_id")
    private Long sentenceId;

    @Column(name = "corpus")
    private String corpus;

}
