package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Collocation;
import com.hugailei.graduation.corpus.domain.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description:
 * </p>
 **/
public interface CollocationDao extends JpaRepository<Collocation, Long> {
}
