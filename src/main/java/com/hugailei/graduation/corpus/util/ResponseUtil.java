package com.hugailei.graduation.corpus.util;


import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.vo.ResponseVO;

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

}