package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.StudentSentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author HU Gailei
 * @date 2018/10/7
 * <p>
 * description:
 * <p/>
 */
@Repository
public interface StudentSentenceDao extends JpaRepository<StudentSentence, Long> {
}
