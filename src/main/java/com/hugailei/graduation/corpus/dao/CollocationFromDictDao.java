package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.CollocationFromDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author HU Gailei
 * @date 2019/1/23
 * <p>
 * description:
 * </p>
 **/
@Repository
public interface CollocationFromDictDao extends JpaRepository<CollocationFromDict, Long> {
    CollocationFromDict findOneByCollocation(String collocation);
}
