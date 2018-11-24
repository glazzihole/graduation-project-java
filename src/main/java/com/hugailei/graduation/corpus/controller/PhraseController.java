package com.hugailei.graduation.corpus.controller;

import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.requesthandlers.RequestHandler;
import nl.inl.blacklab.server.requesthandlers.SentenceRequestHandler;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author HU Gailei
 * @date 2018/11/24
 * <p>
 * description: 短语相关查询
 * </p>
 **/
@RestController
@Slf4j
@RequestMapping("/corpus/phrase")
public class PhraseController {
    private Handler handler = new Handler();
    private BlackLabServer blackLabServer = new BlackLabServer();

    /**
     * 语料索引的前缀
     */
    private static final String INDEX_PREFIX = "phrase-";

    /**
     * 短语检索
     *
     * @param pageable
     * @param request
     * @param response
     * @param corpus
     * @param patt
     */
    @GetMapping("")
    public void searchPhrase (Pageable pageable,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam String corpus,
                                @RequestParam String patt) {
        log.info("searchPhrase | request to search phrase, patt: {}, corpus: {}", patt, INDEX_PREFIX + corpus);
        User user = User.loggedIn("admin", "1");
        handler.checkConfig(request, response, blackLabServer);
        RequestHandler requestHandler = new SentenceRequestHandler(blackLabServer, request, user, INDEX_PREFIX + corpus, null, null);
        handler.checkAndHandler(pageable, corpus, blackLabServer, request, response, requestHandler);
    }
}
