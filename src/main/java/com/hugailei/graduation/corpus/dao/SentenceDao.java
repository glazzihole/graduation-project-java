package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author HU Gailei
 * @date 2018/10/6
 * <p>
 * description:
 * <p/>
 */
public interface SentenceDao extends JpaRepository<Sentence, Long> {
}
