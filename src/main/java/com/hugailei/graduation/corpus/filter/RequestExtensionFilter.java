package com.hugailei.graduation.corpus.filter;

import com.hugailei.graduation.corpus.wrapper.RequestParameterWrapper;
import lombok.extern.slf4j.Slf4j;
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
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("outputformat", "json");
        //利用原始的request对象创建自己扩展的request对象并添加自定义参数
        RequestParameterWrapper requestParameterWrapper = new RequestParameterWrapper(httpServletRequest);
        requestParameterWrapper.addParameters(extraParams);
        filterChain.doFilter(requestParameterWrapper, httpServletResponse);
    }
}
