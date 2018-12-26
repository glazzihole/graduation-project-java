package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.WordWithTopic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author HU Gailei
 * @date 2018/12/25
 * <p>
 * description:
 * </p>
 **/
public interface WordWithTopicDao extends JpaRepository<WordWithTopic, Long> {
}
