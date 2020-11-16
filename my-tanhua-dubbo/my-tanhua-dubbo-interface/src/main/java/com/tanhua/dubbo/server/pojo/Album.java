package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 相册表，用于存储自己发布的数据，每一个用户一张表进行存储。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_album")
public class Album implements java.io.Serializable {

    private ObjectId id;// 主键 id。
    private ObjectId publishId;// 发布 id。
    private Long created;// 发布时间。

}
