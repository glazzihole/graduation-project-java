package com.hugailei.graduation.corpus.util;


import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.service.RankWordService;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author HU Gailei
 * @date 2018/10/5
 * <p>
 * description: 返回数据操作类
 * </p>
 **/
public class ResponseUtil {

    public static ResponseVO success(Object data) {
        return new ResponseVO(CorpusConstant.SUCCESS,
                            CorpusConstant.SUCCESS_CODE,
                            CorpusConstant.SUCCESS,
                            "",
                            data);
    }

    public static ResponseVO success() {
        return new ResponseVO(CorpusConstant.SUCCESS,
                CorpusConstant.SUCCESS_CODE,
                CorpusConstant.SUCCESS,
                "",
                null);
    }

    public static ResponseVO error() {
        return new ResponseVO(CorpusConstant.FAILED,
                CorpusConstant.FAILED_CODE,
                CorpusConstant.FAILED,
                "",
                null);
    }

    public static ResponseVO error(String status, String msg) {
        return new ResponseVO(status,
                            CorpusConstant.FAILED_CODE,
                            msg,
                            "",
                            null);
    }

    public static ResponseVO createPageResponse(List list, Pageable pageable) {
        if (list == null) {
            return error();
        }
        List<Object> page = new ArrayList<>();

        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int totalElements = list.size();
        int totalPages = doubleToInt(Math.ceil((double) totalElements / (double) pageSize));
        int startNumber = pageNumber * pageSize;
        int endNumber = (pageNumber + 1) * pageSize;
        int i = 1;
        for (Object object : list) {
            if (i > startNumber && i <= endNumber) {
                page.add(object);
            }
            i ++;
        }

        ResponseVO.PageInfo pageInfo = new ResponseVO.PageInfo(pageNumber, pageSize, totalPages, totalElements, page);
        return new ResponseVO(CorpusConstant.SUCCESS,
                CorpusConstant.SUCCESS_CODE,
                CorpusConstant.SUCCESS,
                "",
                pageInfo);
    }

    private static int doubleToInt (double d) {
        String numStr = String.valueOf(d).split("\\.")[0];
        int num = Integer.valueOf(numStr);
        return num;
    }
}