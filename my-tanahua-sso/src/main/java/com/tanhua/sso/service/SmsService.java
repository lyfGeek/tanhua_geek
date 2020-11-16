package com.tanhua.sso.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    public static final String REDIS_KEY_PREFIX = "CHECK_CODE_";
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 发送验证码。并存储到 Redis。
     *
     * @param mobile
     * @return
     */
    public Map<String, Object> sendCheckCode(String mobile) {
        Map<String, Object> result = new HashMap<>(2);
        try {
            String redisKey = REDIS_KEY_PREFIX + mobile;
            String value = this.redisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isNotEmpty(value)) {
                result.put("code", 1);
                result.put("msg", "上一次发送的验证码还未失效。");
                return result;
            }
//            String code = this.sendSms(mobile);
            String code = "123456";
            if (null == code) {
                result.put("code", 2);
                result.put("msg", "发送短信验证码失败。");
                return result;
            }
            // 发送验证码成功。
            result.put("code", 3);
            result.put("msg", "ok");

            // 将验证码存储到 Redis，2 分钟后失效。
            this.redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(2));
            return result;
        } catch (Exception e) {
            LOGGER.error("发送验证码出错 ~ " + mobile, e);
            result.put("code", 4);
            result.put("msg", "发送验证码出现异常。");
            return result;
        }
    }

    /**
     * 发送验证码短信。
     *
     * @param mobile
     * @return
     */
    public String sendSms(String mobile) {
        String url = "https://open.ucpaas.com/ol/sms/sendsms";
        Map<String, Object> params = new HashMap<>();
        params.put("sid", "8d76470647c2aac95f2b37be153eccb5");
        params.put("token", "bc28969347578748c40cb84262c39bfe");
        params.put("appid", "66882e6c05ca49269bb6e02fca74db33");
        params.put("templateid", "567772");
        params.put("mobile", mobile);
        // 生成 6 位数验证。
        params.put("param", RandomUtils.nextInt(100000, 999999));
        ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(url, params, String.class);

        String body = responseEntity.getBody();

        try {
            JsonNode jsonNode = MAPPER.readTree(body);
            // 000000 表示发送成功。
            if (StringUtils.equals(jsonNode.get("code").textValue(), "000000")) {
                return String.valueOf(params.get("param"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
