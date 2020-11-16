package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "follow_user")
public class FollowUser implements java.io.Serializable {

    private ObjectId id;// 主键 id。
    private Long userId;// 用户 id。
    private Long followUserId;// 关注的用户 id。
    private Long created;// 关注时间。

}
