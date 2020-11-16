package com.tanhua.server.vo;

import lombok.Data;

@Data
public class PicUploadResult {

    // 文件唯一标识。
    private String uid;
    // 文件名。
    private String name;
    // 状态有：uploading done error removed。
    private String status;
    // 服务端响应内容，如：'{"status": "success"}'。
    private String response;

}
