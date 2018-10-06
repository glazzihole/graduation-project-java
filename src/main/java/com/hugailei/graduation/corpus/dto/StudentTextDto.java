package com.hugailei.graduation.corpus.dto;

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
    private Long stuId;

    private String title;

    @NotNull
    private String text;

    private String type;

    private Long createTime;

    private Long updateTime;
}
