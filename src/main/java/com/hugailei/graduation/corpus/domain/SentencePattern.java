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
     * 主语（主要用于被动句和双宾语句）
     */
    @Column(name = "subject")
    private String subject;

    /**
     * 直接宾语(主要用于双宾语句）
     */
    @Column(name = "direct_object")
    private String directObject;

    /**
     * 间接宾语（主要用于双宾语句）
     */
    @Column(name = "indirect_object")
    private String indirectObject;

    /**
     * 动作发起者（主要用于被动语态）
     */
    @Column(name = "agent")
    private String agent;

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
                           String subject,
                           String directObject,
                           String indirectObject,
                           String agentIndex) {
        this.type = type;
        this.modificand = modificand;
        this.modificandPos = modificandPos;
        this.clauseContent = clauseContent;
        this.subject = subject;
        this.directObject = directObject;
        this.indirectObject = indirectObject;
        this.agent = agentIndex;
    }
}
