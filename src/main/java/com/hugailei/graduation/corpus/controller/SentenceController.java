package com.hugailei.graduation.corpus.controller;

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
 * @date 2018/10/1
 * <p>
 * description: 对句子进行检索
 * </p>
 **/
@RestController
@RequestMapping("/sentence")
public class SentenceController {
    private Handler handler = new Handler();
    private BlackLabServer blackLabServer = new BlackLabServer();

    @GetMapping("/search")
    public void searchSentence (Pageable pageable,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam String corpus,
                                @RequestParam String patt) {
        User user = User.loggedIn("admin", "1");
        handler.checkConfig(request, response, blackLabServer);
        RequestHandler requestHandler = new SentenceRequestHandler(blackLabServer, request, user, corpus, null, null);
        handler.checkAndHandler(pageable, corpus, blackLabServer, request, response, requestHandler);
    }
}
