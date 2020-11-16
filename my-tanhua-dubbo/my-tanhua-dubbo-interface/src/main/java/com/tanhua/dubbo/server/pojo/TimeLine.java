package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 时间线表，用于存储发布（或推荐）的数据，每一个用户一张表进行存储。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_time_line")
public class TimeLine implements java.io.Serializable {

    private ObjectId id;
    private Long userId;// 好友 id。
    private ObjectId publishId;// 发布 id。
    private Long date;// 发布的时间。

}
