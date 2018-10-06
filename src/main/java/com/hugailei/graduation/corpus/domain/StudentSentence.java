package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/10/7
 * <p>
 * description: 学生写作句子信息
 * <p/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_student_sentence")
public class StudentSentence implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "stu_id")
    private Long stuId;

    @Column(name = "sentence")
    private String sentence;

    @Column(name = "text_id")
    private Long textId;
}
