package com.hugailei.graduation.corpus.service;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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

    @Value("${api.translate-url}")
    private String translateApiUrl;

    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    private Map<String, String> translateParams = new HashMap<String, String>() {
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
     * 调用外部机器翻译API，翻译文章内容
     *
     * @param text
     * @param from
     * @param to
     * @return
     */
    public String translate(String text, String from, String to) {
        try {
            log.info("translate | text: {}, from: {}, to: {}", text, from, to);
            String tk = token(text);
            translateParams.put("sl", from);
            translateParams.put("tl", to);
            translateParams.put("tk", tk);
            translateParams.put("q", text);

            URIBuilder uri = new URIBuilder(translateApiUrl);
            for (Map.Entry entry : translateParams.entrySet()) {
                uri.addParameter(entry.getKey().toString(), entry.getValue().toString());
            }
            HttpGet httpGet = new HttpGet(uri.toString());
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity());
            log.info("translate | result: {}", result);

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
