package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movements {

    private String id;// 动态 id。
    private Long userId;// 用户 id。
    private String avatar;// 头像。
    private String nickname;// 昵称
    private String gender;// 性别 man woman。
    private Integer age;// 年龄。
    private String[] tags;// 标签。
    private String textContent;// 文字动态。
    private String[] imageContent;// 图片动态。
    private String distance;// 距离。
    private String createDate;// 发布时间 如：10 分钟前。
    private Integer likeCount;// 点赞数。
    private Integer commentCount;// 评论数。
    private Integer loveCount;// 喜欢数。
    private Integer hasLiked;// 是否点赞（1是，0否）。
    private Integer hasLoved;// 是否喜欢（1是，0否）。

}
