package com.hugailei.graduation.corpus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/10/5
 * <p>
 * description:
 * </p>
 **/
@Data
@AllArgsConstructor
public class DependencyDto implements Serializable {

    private Long id;

    @NotNull
    private String dependencyType;

    @NotNull
    private Integer governorIndex;

    @NotNull
    private String governorLemma;

    @NotNull
    private String governorPos;

    @NotNull
    private Integer dependentIndex;

    @NotNull
    private String dependent;

    @NotNull
    private String dependentPos;

    @NotNull
    private Long sentenceId;
}
