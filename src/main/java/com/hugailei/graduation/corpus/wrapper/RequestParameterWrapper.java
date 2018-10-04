package com.hugailei.graduation.corpus.wrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HU Gailei
 * @date 2018/10/4
 * <p>
 * description: request包装类，给request添加参数
 * </p>
 **/
public class RequestParameterWrapper extends HttpServletRequestWrapper {
    private Map<String, String[]> params = new HashMap<String, String[]>();

    public RequestParameterWrapper(HttpServletRequest request) {
        super(request);
        //将现有parameter传递给params
        this.params.putAll(request.getParameterMap());
    }

    /**
     * 重载构造函数
     * @param request
     * @param extraParams
     */
    public RequestParameterWrapper(HttpServletRequest request, Map<String, Object> extraParams) {
        this(request);
        addParameters(extraParams);
    }

    /**
     * 添加参数
     * @param extraParams
     */
    public void addParameters(Map<String, Object> extraParams) {
        for (Map.Entry<String, Object> entry : extraParams.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                System.out.println(value);
                if (value instanceof String[]) {
                    params.put(name, (String[]) value);
                } else if (value instanceof String) {
                    params.put(name, new String[]{(String) value});
                } else {
                    params.put(name, new String[]{String.valueOf(value)});
                }
            }
        }
    }

    /**
     * 重写getParameter，代表参数从当前类中的map获取
     * @param name
     * @return
     */
    @Override
    public String getParameter(String name) {
        String[]values = params.get(name);
        if(values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    /**
     * 同上
     * @param name
     * @return
     */
    @Override
    public String[] getParameterValues(String name) {
        return params.get(name);
    }
}
