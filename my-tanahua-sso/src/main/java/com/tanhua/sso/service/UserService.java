package com.tanhua.sso.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.sso.mapper.IUserMapper;
import com.tanhua.sso.pojo.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private IUserMapper userMapper;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private HuanXinService huanXinService;

    /**
     * 登录逻辑。
     *
     * @param mobile
     * @param code
     * @return 如果校验成功返回 token，失败返回 null。
     */
    public String login(String mobile, String code) {
        // 校验验证码是否正确。（Redis）。
        String redisKey = "CHECK_CODE_" + mobile;
        String value = this.redisTemplate.opsForValue().get(redisKey);
        // 验证码失效。
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        // 验证码输入错误。
        if (!StringUtils.equals(value, code)) {
            return null;
        }
        // 验证码正确。删除之前的验证码。
        this.redisTemplate.delete(redisKey);

        Boolean isNew = false;// 默认是已注册。

        // 校验该手机号是否已经注册，如果没有注册，需要注册一个账号，如果已经注册，直接登录。
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobile);
        User user = this.userMapper.selectOne(queryWrapper);
        if (null == user) {
            // 该手机号未注册。
            user = new User();
            user.setMobile(mobile);
            // 默认初始密码。
            user.setPassword(DigestUtils.md5Hex("123456"));
            this.userMapper.insert(user);

            isNew = true;

            // 注册用户到环信平台。
//            this.huanXinService.register(user.getId());
        }

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("mobile", mobile);
        claims.put("id", user.getId());

        // 生成 token。
        String token = Jwts.builder()
                .setClaims(claims)// 设置响应数据体。
                .signWith(SignatureAlgorithm.HS256, secret)// 设置加密方法和加密盐。
                .compact();

        // 将 token 存储到 redis 中。
        try {
            String redisTokenKey = "TOKEN_" + token;
            String redisTokenValue = null;// user 的序列化。
            redisTokenValue = MAPPER.writeValueAsString(user);
            this.redisTemplate.opsForValue().set(redisTokenKey, redisTokenValue, Duration.ofHours(1));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            LOGGER.error("存储 Token 出错。", e);
        }

        // RocketMQ 发送消息。
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("id", user.getId());
            msg.put("mobile", mobile);
            msg.put("date", new Date());
            this.rocketMQTemplate.convertAndSend("tanhua-sso-login", msg);
        } catch (MessagingException e) {
            e.printStackTrace();
            LOGGER.error("发送消息出错 ~ ", e);
        }

        return isNew + " ~ " + token;
    }

    /**
     * 根据 Token 查询用户信息。
     *
     * @param token
     * @return
     */
    public User queryUserByToken(String token) {
        try {
            String redisTokenKey = "TOKEN_" + token;
            String cacheData = this.redisTemplate.opsForValue().get(redisTokenKey);
            if (StringUtils.isEmpty(cacheData)) {
                return null;
            }
            // 刷新时间。
            this.redisTemplate.expire(redisTokenKey, 1, TimeUnit.HOURS);
            return MAPPER.readValue(cacheData, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 修改手机号。
     *
     * @param id
     * @param newPhone
     * @return
     */
    public Boolean updateNewMobile(Long id, String newPhone) {
        // 校验新手机号是否已经注册。
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", newPhone);
        User oldUser = this.userMapper.selectOne(queryWrapper);
        if (null != oldUser) {
            // 该手机号已经注册。
            return false;
        }
        User user = new User();
        user.setId(id);
        user.setMobile(newPhone);
        return this.userMapper.updateById(user) > 0;
    }

}
