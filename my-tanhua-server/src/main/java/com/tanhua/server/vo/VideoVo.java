package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoVo {

    private String id;
    private Long userId;
    private String avatar;// 头像。
    private String nickname;// 昵称。
    private String cover;// 封面。
    private String videoUrl;// 视频 URL。
    private String signature;// 签名。
    private Integer likeCount;// 点赞数量。
    private Integer hasLiked;// 是否已赞（1是，0否）。
    private Integer hasFocus;// 是是否关注 （1是，0否）。
    private Integer commentCount;// 评论数量。

}
