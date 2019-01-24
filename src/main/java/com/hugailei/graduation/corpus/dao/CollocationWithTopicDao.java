package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.CollocationWithTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author HU Gailei
 * @date 2018/12/25
 * <p>
 * description:
 * </p>
 **/
@Repository
public interface CollocationWithTopicDao extends JpaRepository<CollocationWithTopic, Long> {
}
