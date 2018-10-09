package com.hugailei.graduation.corpus.dao;

import com.hugailei.graduation.corpus.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/11
 * <p>
 * description:
 * <p/>
 */
public interface WordDao extends JpaRepository<Word, Long>, QueryDslPredicateExecutor<Word> {
}
