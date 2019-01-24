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
 * description: 牛津搭配词典中的搭配
 * </p>
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_dict_collocation")
public class CollocationFromDict implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

    /**
     * 单词
     */
    @Column(name = "word")
    private String word;

    /**
     * 单词词性
     */
    @Column(name = "pos")
    private String pos;

    /**
     * 完整搭配
     */
    @Column(name = "collocation")
    private String collocation;

    /**
     * 搭配词词性
     */
    @Column(name = "collocation_pos")
    private String collocationPos;
}
