package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "recommend_user")
public class RecommendUser implements java.io.Serializable {

    @Id
    private ObjectId id;// 主键 id。
    @Indexed
    private Long userId;// 推荐的用户 id。
    private Long toUserId;// 用户 id。
    @Indexed
    private Double score;// 推荐得分。
    private String date;// 日期。

}
