package com.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 评论表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_comment")
public class Comment implements java.io.Serializable {

    private ObjectId id;
    private ObjectId publishId;// 发布id
    private Integer commentType;// 评论类型，1 ~ 点赞，2 ~ 评论，3 ~ 喜欢。
    private String content;// 评论内容。
    private Long userId;// 评论人。
    private Long publishUserId;// 发布人的用户 id。
    private Boolean isParent = false;// 是否为父节点，默认是否。
    private ObjectId parentId;// 父节点 id。
    private Long created;// 发表时间。

}
