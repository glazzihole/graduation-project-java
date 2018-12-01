package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.dto.CollocationDto;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/26
 * <p>
 * description: 学生作文中搭配查询相关服务
 * </p>
 **/
public interface StudentCollocationService {
    /**
     * 获取文章中的所有搭配信息
     *
     * @param text
     * @return
     */
    CollocationDto.CollocationInfo getCollocationInText(String text);
}
