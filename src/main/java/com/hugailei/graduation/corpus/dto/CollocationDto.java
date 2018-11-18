package com.hugailei.graduation.corpus.dto;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description:
 * </p>
 **/
public class CollocationDto implements Serializable {

    private Long id;

    @NotNull
    private String word;

    private String pos;

    private String collocation;

    private String collocationPos;

    @NotNull
    private int position;

    private String sentenceIds;

    @NotNull
    private String corpus;

    private String type;
}
