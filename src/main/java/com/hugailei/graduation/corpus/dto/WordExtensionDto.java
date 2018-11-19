package com.hugailei.graduation.corpus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/11/19
 * <p>
 * description:
 * </p>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordExtensionDto implements Serializable {

    private Long id;

    private String word;

    private String pos;

    private String relation;

    private String results;
}
