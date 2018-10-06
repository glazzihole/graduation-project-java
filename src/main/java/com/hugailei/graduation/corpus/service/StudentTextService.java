package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.domain.StudentDependency;
import com.hugailei.graduation.corpus.dto.DependencyDto;
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
     * 获取指定id文章的所有依存关系，并存储到数据库中
     *
     * @param textId
     * @return
     */
    List<StudentDependency> getAndSaveDependency(Long textId);

    /**
     * 获取一段文本中的所有依存关系
     *
     * @param text
     * @return
     */
    List<DependencyDto> getDependency(String text);
}
