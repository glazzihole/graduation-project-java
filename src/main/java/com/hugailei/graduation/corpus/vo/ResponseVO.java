package com.hugailei.graduation.corpus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author HU Gailei
 * @date 2018/10/5
 * <p>
 * description: 请求返回格式
 * </p>
 **/
@Data
@AllArgsConstructor
public class ResponseVO implements Serializable {

    private String status;

    private Integer code;

    private String msg;

    private String error;

    private Object data;
}
