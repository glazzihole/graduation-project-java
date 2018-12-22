package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/12/22
 * <p>
 * description: 带主题分类的单词信息
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_word_with_topic")
public class WordWithTopic implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "form")
    private String form;

    @Column(name = "pos")
    private String pos;

    @Column(name = "lemma")
    private String lemma;

    @Column(name = "freq")
    private Integer freq;

    /**
     * 句子的id, 每个id之间用空格隔开
     */
    @Column(name = "sentence_ids")
    @Lob
    private String sentenceIds;

    @Column(name = "corpus")
    private String corpus;

    /**
     * 主题类型
     */
    @Column(name = "topic")
    private Integer topic;

}
