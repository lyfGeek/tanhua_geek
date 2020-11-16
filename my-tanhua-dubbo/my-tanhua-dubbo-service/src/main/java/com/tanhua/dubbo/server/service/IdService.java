package com.tanhua.dubbo.server.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 使用 Redis 的自增长类型，实现自增长的 id。
 */
@Service
public class IdService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 生成自增长的 id。
     *
     * @return
     */
    public Long createId(String type, String objectId) {

        type = StringUtils.upperCase(type);

        String hashKey = "TANHUA_HASH_ID_" + type;
        // 如果 ObjectId 已经存在的话，就返回对应的 id。
        if (this.redisTemplate.opsForHash().hasKey(hashKey, objectId)) {
            return Long.valueOf(this.redisTemplate.opsForHash().get(hashKey, objectId).toString());
        }

        String key = "TANHUA_ID_" + type;
        Long id = this.redisTemplate.opsForValue().increment(key);

        // 将生成的 id 写入到 hash 表中。
        this.redisTemplate.opsForHash().put(hashKey, objectId, id.toString());

        return id;
    }

}
