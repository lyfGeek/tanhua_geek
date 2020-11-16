package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "visitors")
public class Visitors implements java.io.Serializable {

    private ObjectId id;
    private Long userId;// 我的 id。
    private Long visitorUserId;// 来访用户 id。
    private String from;// 来源，如首页、圈子等。
    private Long date;// 来访时间。
    private Double score;// 得分。

}
