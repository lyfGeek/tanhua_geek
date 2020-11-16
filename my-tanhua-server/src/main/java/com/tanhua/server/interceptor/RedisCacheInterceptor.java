package com.tanhua.server.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class RedisCacheInterceptor implements HandlerInterceptor {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${tanhua.cache.enable}")
    private Boolean enable;

    public static String createRedisKey(HttpServletRequest request) throws Exception {
        String paramStr = request.getRequestURI();
        // get 请求可以从？获取参数。
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.isEmpty()) {// 获取不到，是 post 请求。
            // 请求体的数据只能读取一次，需要进行包装 Request 进行解决。
            paramStr += IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        } else {// get 请求可以从？获取参数。
            paramStr += mapper.writeValueAsString(request.getParameterMap());// 序列化转为 json。
        }

        String authorization = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty((authorization))) {
            paramStr += "_" + authorization;
        }

        return "SERVER_DATA_" + DigestUtils.md5Hex(paramStr);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!enable) {
            // 未开启缓存。
            return true;// 直接放行。
        }

        String method = request.getMethod();
//        if (!StringUtils.equalsAnyIgnoreCase(method, "GET", "POST")) {
        if ("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
            // 非 GET、POST 的请求不进行缓存处理。
            return true;
        }

        // 通过缓存做命中，查询 redis，redisKey ? 组成：md5（请求的 url + 请求参数）。
        String redisKey = createRedisKey(request);
        String data = this.redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isEmpty(data)) {
            // 缓存未命中。
            return true;
        }

        // 将 data 数据进行响应。
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(data);

        return false;// 不会去查询数据库 MySQL MongoDB 了。
    }

}
