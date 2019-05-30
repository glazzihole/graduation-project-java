package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.domain.SentencePattern;
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
                                      @RequestParam String patt,
                                      @RequestParam(value = "rank_num", required = false) Integer rankNum) {
        log.info("searchSentenceByPatt | request to search sentence, patt: {}, corpus: {}", patt, corpus);
        User user = User.loggedIn("admin", "1");
        handler.checkConfig(request, response, blackLabServer);
        RequestHandler requestHandler = new SentenceRequestHandler(
                blackLabServer,
                request,
                user,
                corpus,
                null,
                null
        );
        handler.checkAndHandler(pageable, corpus, blackLabServer, request, response, requestHandler);
    }

    /**
     * 按句子ID进行查询
     *
     * @param sentenceIds
     * @param rankNum
     * @param pageable
     * @param request
     * @return
     */
    @PostMapping("/by_id")
    public ResponseVO searchSentenceById(@RequestParam String sentenceIds,
                                         @RequestParam("topic") Integer topic,
                                         @RequestParam(value = "rank_num", required = false) Integer rankNum,
                                         Pageable pageable,
                                         HttpServletRequest request) {
        log.info("searchSentenceById | request to search sentence by ids");
        List<Long> sentenceIdList = Arrays.asList(sentenceIds.split(","))
                .stream()
                .map(i -> Long.valueOf(i))
                .collect(Collectors.toList());
        List<String> result = sentenceService.searchSentenceById(sentenceIdList, topic, rankNum, request);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.createPageResponse(result, pageable);
    }

    /**
     * 获取句型分析结果
     *
     * @param sentence
     * @return
     */
    @PostMapping("/pattern")
    @ResponseBody
    public ResponseVO sentencePattern(@RequestParam String sentence) {
        log.info("sentencePattern | request to get sentence pattern");
        List<SentencePattern> result = sentenceService.getSentencePattern(sentence);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 获取句子中的简单句
     *
     * @param sentence
     * @return
     */
    @PostMapping("/simple-sentence")
    @ResponseBody
    public ResponseVO simpleSentence(@RequestParam String sentence) {
        log.info("simpleSentence | request to get simple sentences");
        List<String> result = sentenceService.getSimpleSentence(sentence);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    @PostMapping("/rank-num")
    @ResponseBody
    public ResponseVO sentenceRankNum(@RequestParam String sentence) {
        log.info("sentenceRankNum | request to get sentence rank num");
        Integer result = sentenceService.getSentenceRankNum(sentence);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }
}
