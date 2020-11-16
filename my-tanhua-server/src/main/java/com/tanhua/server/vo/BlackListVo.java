package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlackListVo {

    private Long id;
    private String avatar;
    private String nickname;
    private String gender;
    private Integer age;

}
