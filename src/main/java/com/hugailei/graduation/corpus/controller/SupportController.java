package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vendor.SupportService;
import com.hugailei.graduation.corpus.vendor.response.YoudaoOpenApiResponse;
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
     * 文本翻译（调用谷歌机器翻译接口）
     *
     * @param text  待翻译内容
     * @param from  源语言
     * @param to    目标语言
     * @return
     */
    @PostMapping("/google-translate")
    @ResponseBody
    public ResponseVO googleTranslate(@RequestParam String text,
                                      @RequestParam String from,
                                      @RequestParam String to) {

        log.info("googleTranslate | request to translate");
        String result = supportService.googleTranslate(text, from, to);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 单词查询或翻译（调用有道词典接口）
     *
     * @param text  待翻译内容
     * @param from  源语言
     * @param to    目标语言
     * @return
     */
    @PostMapping("/youdao-dict")
    @ResponseBody
    public ResponseVO youdaoDict(@RequestParam String text,
                                 @RequestParam String from,
                                 @RequestParam String to) {
        log.info("youdaoDict | request to look up youdao dict");
        YoudaoOpenApiResponse result = supportService.youdaoDict(text, from, to);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);

    }
}
