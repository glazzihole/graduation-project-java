package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.dto.DependencyDto;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.requesthandlers.RequestHandler;
import nl.inl.blacklab.server.requesthandlers.SentenceRequestHandler;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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

    @PostMapping("/dependency")
    @ResponseBody
    public ResponseVO getDependency(Pageable pageable,
                                    @RequestParam String text,
                                    @RequestParam String corpus) {
        List<DependencyDto> dependencyList = null;
    }
}
