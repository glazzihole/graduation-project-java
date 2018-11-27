package com.hugailei.graduation.corpus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class SentencePattern {

    /**
     * 句型：1——主语从句；2——宾语从句；3——定语从句；4——表语从句；5——状语从句；6——同位语从句；7——被动句；8——双宾语句
     */
    private int type;

    /**
     * 从句内容
     */
    private String clauseContent;

    /**
     * 主语的位置（主要用于被动句和双宾语句），从0开始计数
     */
    private Integer subjectIndex;

    /**
     * 直接宾语位置（主要用于双宾语句），从0开始计数
     */
    private Integer directObjectIndex;

    /**
     * 间接宾语位置（主要用于双宾语句），从0开始计数
     */
    private Integer indirectObjectIndex;
}
