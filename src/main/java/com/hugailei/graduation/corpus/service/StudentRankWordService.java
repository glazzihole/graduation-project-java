package com.hugailei.graduation.corpus.service;

import com.hugailei.graduation.corpus.dto.StudentTextDto;

import java.util.Set;

/**
 * @author HU Gailei
 * @date 2019/1/9
 * <p>
 * description: 学生已掌握的等级词汇相关操作
 * </p>
 **/
public interface StudentRankWordService {

    /**
     * 分析学生作文，并存储已掌握的等级词汇，成功返回true，失败返回false;
     *
     * @param studentTextDto
     * @return
     */
    boolean saveRankWordInText(StudentTextDto studentTextDto);

    /**
     * 查询指定学生已掌握的等级大于等于指定等级的所有词汇
     *
     * @param studentId
     * @param rankNum
     * @return
     */
    Set<String> getStudentRankWord(long studentId, int rankNum);
}
