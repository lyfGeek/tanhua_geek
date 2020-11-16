package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Publish;
import com.tanhua.dubbo.server.pojo.TimeLine;
import com.tanhua.dubbo.server.vo.PageInfo;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestQuanzi {

    @Autowired
    private IQuanZiApi quanZiApi;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 发布动态 ~ v1 ~ boolean。
     */
    @Test
    public void testSavePublish() {
        Publish publish = new Publish();
        publish.setUserId(1L);
        publish.setLocationName("上海市");
        publish.setSeeType(1);
        publish.setText("今天天气不错~");
        publish.setMedias(Arrays.asList("http://geek-tanhua.oss-cn-shanghai.aliyuncs.com/images/quanzi/1.jpg"));
        boolean savePublish = this.quanZiApi.savePublishBool(publish);
        System.out.println(savePublish);
    }

    @Test
    public void testRecommendPublish() {
        // 查询用户 id 为 2 的动态作为推荐动态的数据。
        PageInfo<Publish> pageInfo = this.quanZiApi.queryPublishList(2L, 1, 10);
        for (Publish record : pageInfo.getRecords()) {

            TimeLine timeLine = new TimeLine();
            timeLine.setId(ObjectId.get());
            timeLine.setPublishId(record.getId());
            timeLine.setUserId(record.getUserId());
            timeLine.setDate(System.currentTimeMillis());

            this.mongoTemplate.save(timeLine, "quanzi_time_line_recommend");
        }
    }

//    @Test
//    public void testSavePublish2() {
//        for (int i = 0; i < 100; i++) {
//            Publish publish = new Publish();
//            publish.setUserId(RandomUtils.nextLong(1, 10));
//            publish.setPid(Long.valueOf(1000 + i));
//            publish.setLocationName("上海市");
//            publish.setSeeType(1);
//            publish.setText("今天天气不错~ " + i);
//            publish.setMedias(Arrays.asList("http://itcast-tanhua.oss-cn-shanghai.aliyuncs.com/images/2019/10/01/15699262101875720.png",
//                    "http://itcast-tanhua.oss-cn-shanghai.aliyuncs.com/images/2019/10/01/15699262107836768.png",
//                    "http://itcast-tanhua.oss-cn-shanghai.aliyuncs.com/images/2019/10/01/15699262108584571.png",
//                    "http://itcast-tanhua.oss-cn-shanghai.aliyuncs.com/images/2019/10/01/15699262109221190.png"));
//            String publishId = this.quanZiApi.savePublish(publish);
//            System.out.println(publishId);
//        }
//    }

    @Test
    public void testSaveRecommend() {
        for (int i = 0; i < 1000; i++) {
            RecommendQuanZi recommendQuanZi = new RecommendQuanZi();
            recommendQuanZi.setDate(System.currentTimeMillis());
            recommendQuanZi.setId(ObjectId.get());
            recommendQuanZi.setScore(Double.valueOf(RandomUtils.nextInt(0, 15)));
            recommendQuanZi.setUserId(RandomUtils.nextLong(1, 10));
            recommendQuanZi.setPublishId(RandomUtils.nextLong(1000, 1099));

            this.mongoTemplate.save(recommendQuanZi, "recommend_quanzi_20191009");
        }
    }

    @Test
    public void testQueryAlbumList() {
        PageInfo<Publish> pageInfo = this.quanZiApi.queryAlbumList(57L, 1, 10);
        for (Publish record : pageInfo.getRecords()) {
            System.out.println(record);
        }
    }

}
