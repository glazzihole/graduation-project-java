package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.service.StudentCollocationService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/26
 * <p>
 * description: 学生作文中的搭配相关查询
 * </p>
 **/
@RestController
@RequestMapping("/student/collocation")
@Slf4j
public class StudentCollocationController {

    @Autowired
    private StudentCollocationService studentCollocationService;

    @PostMapping("")
    @ResponseBody
    public ResponseVO getCollocationInText(@RequestParam String text) {
        log.info("getCollocationInText | request to get collocation in student's text");
        CollocationDto.CollocationInfo result = studentCollocationService.getCollocationInText(text);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }
}
