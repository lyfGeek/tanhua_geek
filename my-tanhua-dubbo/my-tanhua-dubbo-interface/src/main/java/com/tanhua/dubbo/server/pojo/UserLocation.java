package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_location")
@CompoundIndex(name = "location_index", def = "{'location': '2dsphere'}")
public class UserLocation implements java.io.Serializable {

    @Id
    private ObjectId id;
    @Indexed
    private Long userId;// 用户 id。
    private GeoJsonPoint location;// x ~ 经度 y ~ 纬度。
    private String address;// 位置描述。
    private Long created;// 创建时间。
    private Long updated;// 更新时间。
    private Long lastUpdated;// 上次更新时间。

}
