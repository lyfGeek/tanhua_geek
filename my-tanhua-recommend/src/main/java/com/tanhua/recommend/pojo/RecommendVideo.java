package com.tanhua.recommend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendVideo {

    private ObjectId id;
    private Long userId;// 用户 id。
    private Long videoId;// 视频 id，需要转化为 Long 类型。
    private Double score;// 得分。
    private Long date;// 时间戳。

}
