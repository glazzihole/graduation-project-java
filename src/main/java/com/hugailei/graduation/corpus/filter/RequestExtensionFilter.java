package com.hugailei.graduation.corpus.filter;

import com.hugailei.graduation.corpus.wrapper.RequestParameterWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.hugailei.graduation.corpus.constants.CorpusConstant.REQUEST_ID;

/**
 * @author HU Gailei
 * @date 2018/10/4
 * <p>
 * description: 扩展request请求，添加默认参数outputformat:json
 * </p>
 **/
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
public class RequestExtensionFilter extends OncePerRequestFilter {

    private static final Random RANDOM = new Random();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("outputformat", "json");
        // 利用原始的request对象创建自己扩展的request对象并添加自定义参数
        RequestParameterWrapper requestParameterWrapper = new RequestParameterWrapper(httpServletRequest);
        requestParameterWrapper.addParameters(extraParams);

        // 给请求添加request Id
        String requestId = getRequestId();
        try {
            String xForwardedForHeader = httpServletRequest.getHeader("X-Forwarded-For");
            String remoteIp = httpServletRequest.getRemoteAddr();
            log.info("put requestId ({}) to logger", requestId);
            String[] logParams = new String[]{requestId, remoteIp, xForwardedForHeader};
            log.info("request id:{}, client ip:{}, X-Forwarded-For:{}", logParams);
            MDC.put(REQUEST_ID, requestId);
            filterChain.doFilter(requestParameterWrapper, httpServletResponse);
        } finally {
            MDC.remove(REQUEST_ID);
        }
    }

    private String getRequestId() {
        return String.valueOf(System.currentTimeMillis()) + String.format("%02d", RANDOM.nextInt(100));
    }

}
