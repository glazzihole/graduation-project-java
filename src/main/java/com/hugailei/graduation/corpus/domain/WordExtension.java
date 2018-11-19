package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/11/19
 * <p>
 * description: 单词的扩展信息，包括同义词，近义词，反义词，相关词等
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_word_extension")
public class WordExtension implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "word")
    private String word;

    @Column(name = "pos")
    private String pos;

    @Column(name = "relation")
    private String relation;

    @Column(name = "results")
    @Lob
    private String results;
}
