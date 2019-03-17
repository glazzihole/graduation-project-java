package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author HU Gailei
 * @date 2019/3/16
 * <p>
 * description: 用户登录
 * </p>
 **/
@RequestMapping("/login")
@RestController
@Slf4j
public class LogInController {

    @PostMapping
    public ResponseVO logIn(@RequestParam("student_id") Long studentId,
                            HttpServletRequest request) {
        log.info("logIn | request to log in");
        request.getSession().setAttribute("student_id", studentId);
        return ResponseUtil.success();
    }
}
