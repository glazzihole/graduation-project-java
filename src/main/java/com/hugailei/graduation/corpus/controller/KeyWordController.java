package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.dto.WordDto;
import com.hugailei.graduation.corpus.service.KeyWordService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/8
 * <p>
 * description: 关键词查询
 * </p>
 **/
@RestController
@Slf4j
@RequestMapping("/corpus/keyword")
public class KeyWordController {

    @Autowired
    private KeyWordService keyWordService;

    /**
     * keyword 列表查询
     *
     * @param corpus    指定语料库
     * @param refCorpus 参考语料库
     * @param rankNum   等级
     * @param pageable
     * @return
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseVO keyWordList(@RequestParam String corpus,
                                  @RequestParam("ref_corpus") String refCorpus,
                                  @RequestParam(value = "rank_num", required = false) Integer rankNum,
                                  Pageable pageable) {
        log.info("keyWordList | request to get keyword list");
        List<WordDto> result = keyWordService.keyWordList(corpus, refCorpus, rankNum);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.createPageResponse(result, pageable);
    }
}
