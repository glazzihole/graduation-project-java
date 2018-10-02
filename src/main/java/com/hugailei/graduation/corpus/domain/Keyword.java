package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
public class Keyword implements Serializable {
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "word", nullable = false)
    private String word;
}
