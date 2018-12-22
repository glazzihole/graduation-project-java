package com.hugailei.graduation.corpus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HU Gailei
 * @date 2018/12/21
 * <p>
 * description:
 * </p>
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TopicDto {
    /**
     * 主题名称
     */
    private String topicName;

    /**
     * 主题标号
     */
    private Integer topicNum;

    /**
     * 为当前主题的概率
     */
    private Float prob;
}
