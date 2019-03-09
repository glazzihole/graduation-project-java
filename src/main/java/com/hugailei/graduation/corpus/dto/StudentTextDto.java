package com.hugailei.graduation.corpus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/10/6
 * <p>
 * description:
 * <p/>
 */
@Data
public class StudentTextDto implements Serializable {

    private Long id;

    @NotNull
    @JsonProperty("student_id")
    private Long studentId;

    private String title;

    @NotNull
    private String text;

    private String topic;

    @NotNull
    @JsonProperty("rank_num")
    private Integer rankNum;

    @JsonProperty("create_time")
    private Long createTime;

    @JsonProperty("update_time")
    private Long updateTime;
}
