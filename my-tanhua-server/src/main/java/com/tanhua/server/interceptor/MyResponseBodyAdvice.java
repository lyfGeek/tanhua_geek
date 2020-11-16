package com.tanhua.server.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Duration;

/**
 * RedisCacheInterceptor 完成了缓存命中逻辑。
 * 在查到数据后，如何将结果写入缓存？
 * 不能通过拦截器实现。拦截器有 3 个执行节点：执行 Controller 之前，之后，完成之后。完成之后就已经完成了视图渲染。
 * 不能拿到响应结果了。
 * 那就通过 ResponseBodyAdvice 实现。Spring 提供的高级用法。
 * controller 执行返回结果前。会在结果被处理前进行拦截。拦截的逻辑自己实现。
 */
@ControllerAdvice
public class MyResponseBodyAdvice implements ResponseBodyAdvice {

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * @param methodParameter returnGType。
     * @param aClass          converterType。
     * @return
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
//        return false;
        // 支持的处理类型，这里仅对 get 和 post 进行处理。
        return methodParameter.hasMethodAnnotation(GetMapping.class) || methodParameter.hasMethodAnnotation(PostMapping.class);
    }

    /**
     * 渲染给前端之前的处理。
     *
     * @param body
     * @param methodParameter
     * @param mediaType
     * @param aClass
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
//        return null;
        String redisKey = null;
        try {
            redisKey = RedisCacheInterceptor.createRedisKey(((ServletServerHttpRequest) serverHttpRequest).getServletRequest());
            String redisValue;
            // 如果不是字符串，序列化为 json。
            if (body instanceof String) {
                redisValue = (String) body;
            } else {
                redisValue = mapper.writeValueAsString(body);
            }
            // 存储到 redis 中。
            this.redisTemplate.opsForValue().set(redisKey, redisValue, Duration.ofHours(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return body;
    }

}
