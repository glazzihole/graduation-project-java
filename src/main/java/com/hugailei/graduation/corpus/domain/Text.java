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

//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "text", nullable = false)
    @Lob
    private String text;

    @Column(name = "title")
    private String title;

    /**
     * 主题类型
     */
    @Column(name = "topic")
    private Integer topic;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tb_text_keyword",
            joinColumns = {@JoinColumn(name = "text_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "keyword_id", referencedColumnName = "id")}
    )
    private Set<KeyWord> keywords = new HashSet<>();

    @Column(name = "sentence_count")
    private Integer sentenceCount;

    @Column(name = "corpus", nullable = false)
    private String corpus;
}
