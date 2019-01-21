package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.StudentRankWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2019/1/9
 * <p>
 * description:
 * </p>
 **/
@Repository
public interface StudentRankWordDao extends JpaRepository<StudentRankWord, Long> {
    /**
     * 根据学生ID和等级查找学生所有写作记录中已经掌握的大于等于指定等级的词汇
     *
     * @param studentId 学生ID
     * @param rankNum   指定等级
     * @return
     */
    List<StudentRankWord> findByStudentIdAndRankNumIsGreaterThanEqual(long studentId, int rankNum);

    /**
     * 根据学生ID和等级查找学生在指定等级下的已经掌握的大于等于指定等级的词汇
     *
     * @param studentId 学生ID
     * @param rankNum   指定等级
     * @return
     */
    StudentRankWord findOneByStudentIdAndRankNum(long studentId, int rankNum);
}
