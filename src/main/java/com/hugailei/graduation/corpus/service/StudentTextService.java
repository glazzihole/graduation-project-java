package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.dto.SentencePatternDto;
import com.hugailei.graduation.corpus.dto.StudentTextDto;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/6
 * <p>
 * description: 学生作文相关查询及存储操作
 * <p/>
 */
public interface StudentTextService {


    /**
     * 存储学生作文
     *
     * @param studentTextDto
     * @return
     */
    StudentTextDto insertText(StudentTextDto studentTextDto);

    /**
     * 获取学生作文
     *
     * @param textId
     * @return
     */
    StudentTextDto getStudentText(Long textId);

    /**
     * 获取文章中的所有搭配信息
     *
     * @param text
     * @return
     */
    CollocationDto.CollocationInfo getCollocationInText(String text);

    /**
     * 获取文章中的所有句型信息
     *
     * @param text
     * @return
     */
    List<SentencePatternDto> getSentencePatternInText(String text);
}
