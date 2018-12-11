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
 * description: 文章的关键词
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_keyword")
public class KeyWord implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "word", nullable = false)
    private String word;

    @Column(name = "lemma")
    private String lemma;

    @Column(name = "pos")
    private String pos;

    @Column(name = "corpus", nullable = false)
    private String corpus;

    @Column(name = "ref_corpus", nullable = false)
    private String refCorpus;

    @Column(name = "keyness", nullable = false)
    private Double keyness;
}
