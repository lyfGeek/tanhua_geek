package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 发布表，动态内容。
 * <p>
 * 发布流程。
 * <p>
 * 发布动态。
 * <p>
 * -> 相册表 ~ 存储发布表的指向。
 * -> 发布表。
 * -> 时间线表 ~ 存储发布表的执行。
 * <p>
 * 用户发布动态，首先将动态内容写入到发布表。
 * 然后，将发布的指向写入到自己的相册表中。
 * 最后，将发布的指向写入到好友的时间线中。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_publish")
public class Publish implements java.io.Serializable {

    private ObjectId id;// 主键 id。
    private Long pid;// Long 类型，用于推荐系统的模型。
    private Long userId;
    private String text;// 文字。
    private List<String> medias;// 媒体数据，图片或小视频 url。
    private Integer seeType;// 谁可以看，1 ~ 公开，2 ~ 私密，3 ~ 部分可见，4 ~ 不给谁看。
    private List<Long> seeList;// 部分可见的列表。
    private List<Long> notSeeList;// 不给谁看的列表。
    private String longitude;// 经度。
    private String latitude;// 纬度。
    private String locationName;// 位置名称。
    private Long created;// 发布时间。

}
