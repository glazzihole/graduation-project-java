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
 * description: 学生作文中中的句法依存关系
 * <p/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_student_dependency")
public class StudentDependency implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "stu_id", nullable = false)
    private Long stuId;

    @Column(name = "dependency_type", nullable = false)
    private String dependencyType;

    @Column(name = "governor_index", nullable = false)
    private Integer governorIndex;

    @Column(name = "governor", nullable = false)
    private String governorLemma;

    @Column(name = "governor_pos", nullable = false)
    private String governorPos;

    @Column(name = "dependent_index", nullable = false)
    private Integer dependentIndex;

    @Column(name = "dependent", nullable = false)
    private String dependent;

    @Column(name = "dependent_pos", nullable = false)
    private String dependentPos;

    @Column(name = "sentence_id", nullable = false)
    private Long sentenceId;
}
