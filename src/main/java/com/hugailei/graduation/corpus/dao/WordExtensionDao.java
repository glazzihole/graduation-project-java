package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.WordExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author HU Gailei
 * @date 2018/11/19
 * <p>
 * description:
 * </p>
 **/
@Repository
public interface WordExtensionDao extends JpaRepository<WordExtension, Long> {
}
