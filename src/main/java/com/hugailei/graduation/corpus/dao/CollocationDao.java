package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Collocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description:
 * </p>
 **/
@Repository
public interface CollocationDao extends JpaRepository<Collocation, Long> {
}
