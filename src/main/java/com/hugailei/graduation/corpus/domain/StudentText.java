package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/10/6
 * <p>
 * description: 学生作文
 * <p/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_student_text")
public class StudentText implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "title")
    private String title;

    @Column(name = "text")
    @Lob
    private String text;

    @Column(name = "topic")
    private String topic;

    @Column(name = "rank_num")
    private Integer rankNum;

    @Column(name = "create_time")
    private Long createTime;

    @Column(name = "update_time")
    private Long updateTime;
}
