package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * @author HU Gailei
 * @date 2018/10/11
 * <p>
 * description:
 * <p/>
 */
@Repository
public interface WordDao extends JpaRepository<Word, Long>{
}
