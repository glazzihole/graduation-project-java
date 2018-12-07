package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/11/27
 * <p>
 * description: 句型相关
 * </p>
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "tb_sentence_pattern")
public class SentencePattern implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 句型：1——主语从句；2——宾语从句；3——定语从句或同位语从句；4——表语从句；5——状语从句；6——被动句；7——双宾语句
     */
    @Column(name = "type", length = 1)
    private Integer type;

    /**
     * 被从句所修饰的词，主要用于定语从句或同位语从句
     */
    @Column(name = "modificand")
    private String modificand;

    /**
     * 被从句所修饰的词的词性，主要用于定语从句或同位语从句
     */
    @Column(name = "modificand_pos")
    private String modificandPos;

    /**
     * 从句内容
     */
    @Column(name = "clause_content")
    @Lob
    private String clauseContent;

    /**
     * 主语的位置（主要用于被动句和双宾语句），从0开始计数
     */
    @Column(name = "subject_index", length = 3)
    private Integer subjectIndex;

    /**
     * 直接宾语位置（主要用于双宾语句），从0开始计数
     */
    @Column(name = "direct_object_index", length = 3)
    private Integer directObjectIndex;

    /**
     * 间接宾语位置（主要用于双宾语句），从0开始计数
     */
    @Column(name = "indirect_object_index", length = 3)
    private Integer indirectObjectIndex;

    /**
     * 动作发起者位置（主要用于被动语态）
     */
    @Column(name = "agent_index", length = 3)
    private Integer agentIndex;

    @Column(name = "freq")
    private Integer freq;

    @Column(name = "sentence_ids")
    @Lob
    private String sentenceIds;

    @Column(name = "corpus")
    private String corpus;

    public SentencePattern(Integer type,
                           String modificand,
                           String modificandPos,
                           String clauseContent,
                           Integer subjectIndex,
                           Integer directObjectIndex,
                           Integer indirectObjectIndex,
                           Integer agentIndex) {
        this.type = type;
        this.modificand = modificand;
        this.modificandPos = modificandPos;
        this.clauseContent = clauseContent;
        this.subjectIndex = subjectIndex;
        this.directObjectIndex = directObjectIndex;
        this.indirectObjectIndex = indirectObjectIndex;
        this.agentIndex = agentIndex;
    }
}
