package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comments {

    private String id;// 评论 id。
    private String avatar;// 头像。
    private String nickname;// 昵称。
    private String content;// 评论。
    private String createDate;// 评论时间：08:27。
    private Integer likeCount;// 点赞数。
    private Integer hasLiked;// 是否点赞（1是，0否）。

}
