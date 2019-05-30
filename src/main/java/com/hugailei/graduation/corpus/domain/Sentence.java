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
 * description: 句子信息
 * </p>
 **/
@Entity
@Table(name = "tb_sentence",
        indexes = {
                @Index(name = "id", columnList = "id", unique = true),
                @Index(name = "text_id", columnList = "text_id")
        })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sentence implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "sentence")
    @Lob
    private String sentence;

    @Column(name = "text_id")
    private Long textId;

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "topic")
    private Integer topic;

    @Column(name = "corpus")
    private String corpus;
}
