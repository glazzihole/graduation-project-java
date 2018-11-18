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
 * description: 词语搭配
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_collocation")
public class Collocation implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 单词
     */
    @Column(name = "word")
    private String word;

    /**
     * 词性
     */
    @Column(name = "pos")
    private String pos;

    /**
     * 搭配
     */
    @Column(name = "collocation")
    private String collocation;

    /**
     * 搭配词词性
     */
    @Column(name = "collocation_pos")
    private String collocationPos;

    /**
     * 词前搭配/词后搭配，0——词前搭配，1——词后搭配
     */
    @Column(name = "position", length = 1)
    private int position;

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
    @Column(name = "type")
    private String type;
}
