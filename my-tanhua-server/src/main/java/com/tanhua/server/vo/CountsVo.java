package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountsVo {

    private Long eachLoveCount;// 互相喜欢。
    private Long loveCount;// 喜欢。
    private Long fanCount;// 粉丝。

}
