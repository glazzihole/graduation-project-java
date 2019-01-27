package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description: 带主题分类的词语搭配信息
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_collocation_with_topic")
public class CollocationWithTopic implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 搭配中的第一个单词
     */
    @Column(name = "first_word", length = 30)
    private String firstWord;

    /**
     * 搭配中的第一个单词的词性
     */
    @Column(name = "first_pos", length = 5)
    private String firstPos;

    /**
     * 搭配中的第二个单词
     */
    @Column(name = "second_word", length = 30)
    private String secondWord;

    /**
     * 搭配中的第二个单词的词性
     */
    @Column(name = "second_pos", length = 5)
    private String secondPos;

    /**
     * 搭配中的第三个单词
     */
    @Column(name = "third_word", length = 30)
    private String thirdWord;

    /**
     * 搭配中的第三个单词的词性
     */
    @Column(name = "third_pos", length = 5)
    private String thirdPos;

    /**
     * 该搭配在语料库中出现的频次
     */
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
