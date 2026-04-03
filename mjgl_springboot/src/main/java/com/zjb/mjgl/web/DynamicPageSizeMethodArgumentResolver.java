package com.zjb.mjgl.web;

import com.zjb.mjgl.common.BusinessConfigKeys;
import com.zjb.mjgl.service.SystemBusinessConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class DynamicPageSizeMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final SystemBusinessConfigService systemBusinessConfigService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(DynamicPageSize.class)
                && parameter.getParameterType() == int.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String raw = webRequest.getParameter("pageSize");
        if (raw != null && !raw.trim().isEmpty()) {
            try {
                int p = Integer.parseInt(raw.trim());
                if (p > 0) {
                    return p;
                }
            } catch (NumberFormatException ignored) {
                // fall through to default
            }
        }
        return systemBusinessConfigService.getEffectiveInt(BusinessConfigKeys.PAGINATION_DEFAULT_PAGE_SIZE);
    }
}
