package com.hugailei.graduation.corpus.vendor;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hugailei.graduation.corpus.util.Md5EncryptUtil;
import com.hugailei.graduation.corpus.vendor.response.YoudaoOpenApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HU Gailei
 * @date 2018/11/17
 * <p>
 * description: 提供一些支持性服务
 * </p>
 **/
@Slf4j
@Service
public class SupportService {

    @Value("${vendor.google-translate-url}")
    private String googleTranslateApiUrl;

    @Value("${vendor.youdao.open-api-url}")
    private String youdaoOpenApiUrl;

    @Value("${vendor.youdao.app-key}")
    private String youdaoAppKey;

    @Value("${vendor.youdao.app-secret}")
    private String youdaoAppSecret;

    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    private Map<String, String> googleTranslateParams = new HashMap<String, String>() {
        {
            put("client", "t");
            put("hl", "zh-CN");
            put("dt", "at");
            put("dt", "bd");
            put("dt", "ex");
            put("dt", "ld");
            put("dt", "md");
            put("dt", "qca");
            put("dt", "rw");
            put("dt", "rm");
            put("dt", "ss");
            put("dt", "t");
            put("ie", "UTF-8");
            put("oe", "UTF-8");
            put("source", "btn");
            put("ssel", "0");
            put("tsel", "0");
            put("kc", "0");
        }
    };

    /**
     * 调用谷歌机器翻译API，翻译文章内容
     *
     * @param text
     * @param from
     * @param to
     * @return
     */
    public String googleTranslate(String text, String from, String to) {
        try {
            log.info("googleTranslate | text: {}, from: {}, to: {}", text, from, to);
            String tk = token(text);

            Map<String, String> paramMap = new HashMap<>();
            for (Map.Entry entry : googleTranslateParams.entrySet()) {
                paramMap.put((String)entry.getKey(), (String)entry.getValue());
            }
            paramMap.put("sl", from);
            paramMap.put("tl", to);
            paramMap.put("tk", tk);
            paramMap.put("q", text);

            URIBuilder uri = new URIBuilder(googleTranslateApiUrl);
            for (Map.Entry entry : paramMap.entrySet()) {
                uri.addParameter(entry.getKey().toString(), entry.getValue().toString());
            }
            HttpGet httpGet = new HttpGet(uri.toString());
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity());
            log.info("googleTranslate | result: {}", result);

            JSONArray resultArray = (JSONArray)JSONArray.parse(result);
            JSONArray array = resultArray.getJSONArray(0);
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < array.size(); i++) {
                String sentence = array.getJSONArray(i).getString(0);
                if (StringUtils.isNotBlank(sentence)) {
                    stringBuffer.append(sentence);
                }
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            log.error("error: {}", e);
            return null;
        }
    }

    /**
     * 单词查询或翻译（调用有道词典接口）
     *
     * @param text
     * @param from
     * @param to
     * @return
     */
    public YoudaoOpenApiResponse youdaoDict(String text, String from, String to) {
        try {
            log.info("youdaoDict | text: {}, from: {}, to: {}", text, from, to);
            String salt = String.valueOf(System.currentTimeMillis());
            String sign = Md5EncryptUtil.md5Encrypt(youdaoAppKey + text + salt + youdaoAppSecret);
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("q", text);
            paramMap.put("from", from);
            paramMap.put("to", to);
            paramMap.put("sign", sign);
            paramMap.put("salt", salt);
            paramMap.put("appKey", youdaoAppKey);

            URIBuilder uri = new URIBuilder(youdaoOpenApiUrl);
            for (Map.Entry entry : paramMap.entrySet()) {
                uri.addParameter(entry.getKey().toString(), entry.getValue().toString());
            }
            HttpPost httpPost = new HttpPost(uri.toString());
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity());
            YoudaoOpenApiResponse youdaoOpenApiResponse = new ObjectMapper().readValue(result.getBytes(), YoudaoOpenApiResponse.class);
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK || !"0".equals(youdaoOpenApiResponse.getErrorCode())){
                return null;
            }
            return youdaoOpenApiResponse;
        } catch (Exception e) {
            log.error("youdaoDict | error: {}", e);
            return null;
        }
    }


    private String token(String text) throws Exception{
        String result = "";
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        try {
            Resource resource = new ClassPathResource("google.js");
            FileReader reader = new FileReader(resource.getFile());
            engine.eval(reader);
            if (engine instanceof Invocable) {
                Invocable invoke = (Invocable)engine;
                result = String.valueOf(invoke.invokeFunction("token", text));
            }
        } catch (Exception e) {
            log.error("token | error: {}", e);
            throw new Exception(e);
        }
        return result;
    }
}
