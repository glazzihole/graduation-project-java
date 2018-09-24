package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author HU Gailei
 * @date 2018/9/24
 * <p>
 * description: 文章信息
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_text")
public class Text implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "corpus", nullable = false)
    private String corpus;

    @Column(name = "title")
    private String title;

    @Column(name = "type")
    private String type;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tb_text_keyword",
            joinColumns = {@JoinColumn(name = "text_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "keyword_id", referencedColumnName = "id")}
    )
    private Set<Keyword> keywords = new HashSet<>();

    @Column(name = "sentence_count")
    private Integer sentenceCount;

}
