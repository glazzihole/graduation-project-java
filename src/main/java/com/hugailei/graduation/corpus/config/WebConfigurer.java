package com.hugailei.graduation.corpus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author HU Gailei
 * @date 2019/5/15
 * <p>
 * description:
 * </p>
 **/
@Configuration
public class WebConfigurer extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //指定静态目录位置
        registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/templates/assets/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/templates/css/");
        registry.addResourceHandler("/image/**").addResourceLocations("classpath:/templates/image/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/templates/js/");
        super.addResourceHandlers(registry);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/corpus/home").setViewName("index");
        registry.addViewController("/corpus/writing").setViewName("writing");
        super.addViewControllers(registry);
    }
}
