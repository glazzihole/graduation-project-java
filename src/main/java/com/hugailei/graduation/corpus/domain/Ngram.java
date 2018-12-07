package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/10/27
 * <p>
 * description: ngram信息
 * </p>
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_ngram")
public class Ngram implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

    /**
     * ngram 单词串
     */
    @Column(name = "ngram_str")
    private String ngramStr;

    /**
     * n值
     */
    @Column(name = "n_value")
    private int nValue;

    /**
     * ngram出现的频次
     */
    @Column(name = "freq")
    private int freq;

    @Column(name = "corpus")
    private String corpus;
}
