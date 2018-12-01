package com.hugailei.graduation.corpus.vendor.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/28
 * <p>
 * description: 有道词典接口响应类
 * </p>
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YoudaoOpenApiResponse {

    @JsonProperty("errorCode")
    public String errorCode;

    @JsonProperty("query")
    public String query;

    @JsonProperty("translation")
    public List<String> translation;

    @JsonProperty("basic")
    public Basic basic;

    @JsonProperty("web")
    public List<Web> web;

    @JsonProperty("dict")
    public Dict dict;

    @JsonProperty("webdict")
    public WebDict webDict;

    @JsonProperty("l")
    public String l;

    /**
     * 翻译后的发音地址
     */
    @JsonProperty("tSpeakUrl")
    public String tSpeakUrl;

    /**
     * 查询文本的发音地址
     */
    @JsonProperty("speakUrl")
    public String speakUrl;


    /**
     * 有道词典-基本词典,查词时才有
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Basic {

        @JsonProperty("phonetic")
        public String phonetic;

        /**
         * 英式音标
         */
        @JsonProperty("uk-phonetic")
        public String ukPhonetic;

        /**
         * 美式音标
         */
        @JsonProperty("us-phonetic")
        public String usPhonetic;

        /**
         * 英式发音
         */
        @JsonProperty("uk-speech")
        public String ukSpeech;

        /**
         * 美式音标
         */
        @JsonProperty("us-speech")
        public String usSpeech;

        @JsonProperty("explains")
        public List<String> explains;
    }

    /**
     * 有道词典-网络释义，该结果不一定存在
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Web {
        @JsonProperty("key")
        public String key;

        @JsonProperty("value")
        public List<String> value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dict {
        @JsonProperty("url")
        public String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebDict {
        @JsonProperty("url")
        public String url;
    }
}
