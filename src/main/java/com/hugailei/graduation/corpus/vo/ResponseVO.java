package com.hugailei.graduation.corpus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

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

    @Data
    @AllArgsConstructor
    public static class PageInfo implements Serializable {
        private int pageNumber;

        private int pageSize;

        private int totalPages;

        private int totalElements;

        private List<Object> page;
    }
}
