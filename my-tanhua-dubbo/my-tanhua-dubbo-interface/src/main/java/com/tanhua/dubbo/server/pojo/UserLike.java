package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_like")
public class UserLike implements java.io.Serializable {

    private ObjectId id;
    @Indexed
    private Long userId;// 用户 id，自己。
    @Indexed
    private Long likeUserId;// 喜欢的用户 id，对方。
    private Long created;// 创建时间。

}
