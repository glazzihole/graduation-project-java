package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.dto.SentenceDto;
import com.hugailei.graduation.corpus.service.SentenceService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.requesthandlers.RequestHandler;
import nl.inl.blacklab.server.requesthandlers.SentenceRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description: 对语料库中的句子进行查询
 * </p>
 **/
@RestController
@RequestMapping("/corpus/sentence")
@Slf4j
public class SentenceController {
    private Handler handler = new Handler();
    private BlackLabServer blackLabServer = new BlackLabServer();

    @Autowired
    private SentenceService sentenceService;

    /**
     * 句子检索
     *
     * @param pageable
     * @param request
     * @param response
     * @param corpus
     * @param patt
     */
    @GetMapping
    public void searchSentenceByPatt (Pageable pageable,
                                      HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam String corpus,
                                      @RequestParam String patt) {
        log.info("searchSentenceByPatt | request to search sentence, patt: {}, corpus: {}", patt, corpus);
        User user = User.loggedIn("admin", "1");
        handler.checkConfig(request, response, blackLabServer);
        RequestHandler requestHandler = new SentenceRequestHandler(blackLabServer, request, user, corpus, null, null);
        handler.checkAndHandler(pageable, corpus, blackLabServer, request, response, requestHandler);
    }

    @PostMapping("/by_id")
    public ResponseVO searchSentenceById(@RequestParam String sentenceIds,
                                         Pageable pageable) {
        log.info("searchSentenceById | request to search sentence by ids");
        List<Long> sentenceIdList = Arrays.asList(sentenceIds.split(",")).stream().map(i -> Long.valueOf(i)).collect(Collectors.toList());
        List<SentenceDto> result = sentenceService.searchSentenceById(sentenceIdList);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.createPageResponse(result, pageable);
    }

    /**
     * 句子过滤，只保留包含指定关键字的例句
     *
     * @param keyword       关键字
     * @param sentenceIds   例句集合
     * @param corpus        语料库名称
     * @param pageable
     * @return
     */
    @GetMapping("/filter")
    @ResponseBody
    public ResponseVO filterSentence(@RequestParam String keyword,
                                     @RequestParam("sentence_ids") String sentenceIds,
                                     @RequestParam String corpus,
                                     Pageable pageable) {
        log.info("filterSentence | request to filter sentences");
        List<Long> sentenceIdList = Arrays.asList(sentenceIds.split(",")).stream().map(i -> Long.valueOf(i)).collect(Collectors.toList());
        List<SentenceDto> result = sentenceService.filterSentence(keyword, sentenceIdList, corpus);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.createPageResponse(result, pageable);
    }
}
