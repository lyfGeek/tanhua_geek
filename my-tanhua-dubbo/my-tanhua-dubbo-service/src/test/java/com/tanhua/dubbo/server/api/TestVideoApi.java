package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Video;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestVideoApi {

    @Autowired
    private IVideoApi videoApi;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testSaveVideo() {
        for (int i = 0; i < 100; i++) {
            Video video = new Video();
            video.setId(ObjectId.get());
            video.setUserId(RandomUtils.nextLong(1, 10));
            video.setVid(Long.valueOf(1000 + i));
            video.setLocationName("上海市");
            video.setSeeType(1);
            video.setPicUrl("http://itcast-tanhua.oss-cn-shanghai.aliyuncs.com/images/2019/10/07/15704367549907417.png");
            video.setVideoUrl("http://192.168.33.128:8888/group1/M00/00/02/wKgfUV2a9pmAQgIlACYJuhDRF3g362.mp4");
            video.setCreated(System.currentTimeMillis());
            String videoId = this.videoApi.saveVideo(video);
            System.out.println(videoId);
        }
    }

    @Test
    public void testSaveRecommend() {
        for (int i = 0; i < 1000; i++) {
            RecommendVideo recommendQuanZi = new RecommendVideo();
            recommendQuanZi.setDate(System.currentTimeMillis());
            recommendQuanZi.setId(ObjectId.get());
            recommendQuanZi.setScore(Double.valueOf(RandomUtils.nextInt(0, 15)));
            recommendQuanZi.setUserId(RandomUtils.nextLong(1, 10));
            recommendQuanZi.setVideoId(RandomUtils.nextLong(1000, 1099));

            this.mongoTemplate.save(recommendQuanZi, "recommend_video_20191010");
        }
    }

}
