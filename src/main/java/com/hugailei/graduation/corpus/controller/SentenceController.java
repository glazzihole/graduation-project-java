package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.service.SentenceService;
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

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description: 对语料库中的句子进行存储、检索
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
    @GetMapping("/search")
    public void searchSentence (Pageable pageable,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam String corpus,
                                @RequestParam String patt) {
        log.info("searchSentence | request to search sentence, patt: {}, corpus: {}", patt, corpus);
        User user = User.loggedIn("admin", "1");
        handler.checkConfig(request, response, blackLabServer);
        RequestHandler requestHandler = new SentenceRequestHandler(blackLabServer, request, user, corpus, null, null);
        handler.checkAndHandler(pageable, corpus, blackLabServer, request, response, requestHandler);
    }
}
