package com.hugailei.graduation.corpus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * @author HU Gailei
 * @date 2018/12/16
 * <p>
 * description: 句型信息
 * </p>
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SentencePatternDto implements Serializable {
    /**
     * 句型类型名称
     */
    private String patternTypeName;

    /**
     * 句型出现次数
     */
    private int freq;

    /**
     * 包含该句型的句子列表
     */
    private Set<String> sentenceSet;
}
