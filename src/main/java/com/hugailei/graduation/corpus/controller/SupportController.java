package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.service.SupportService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author HU Gailei
 * @date 2018/11/17
 * <p>
 * description: 提供一些支持性接口
 * </p>
 **/
@RestController
@RequestMapping("/support")
@Slf4j
public class SupportController {

    @Autowired
    private SupportService supportService;

    /**
     * 文本翻译
     *
     * @param text  待翻译内容
     * @param from  源语言
     * @param to    目标语言
     * @return
     */
    @PostMapping("/translate")
    @ResponseBody
    public ResponseVO translate(@RequestParam String text,
                                @RequestParam String from,
                                @RequestParam String to) {

        log.info("translate | request to translate");
        String result = supportService.translate(text, from, to);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }
}
