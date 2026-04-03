package com.zjb.mjgl.config;

import com.zjb.mjgl.web.DynamicPageSizeMethodArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcBusinessConfig implements WebMvcConfigurer {

    private final DynamicPageSizeMethodArgumentResolver dynamicPageSizeMethodArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(0, dynamicPageSizeMethodArgumentResolver);
    }
}
