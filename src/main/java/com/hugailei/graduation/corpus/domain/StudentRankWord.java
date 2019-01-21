package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2019/1/9
 * <p>
 * description: 学生已掌握的等级词汇
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_student_rank_word")
@IdClass(value = StudentRankWord.class)
public class StudentRankWord implements Serializable {

    @Column(name = "student_id")
    @Id
    private Long studentId;

    @Column(name = "rank_num")
    @Id
    private Integer rankNum;

    @Column(name = "words")
    @Lob
    private String words;
}
