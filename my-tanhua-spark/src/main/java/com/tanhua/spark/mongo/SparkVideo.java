package com.tanhua.spark.mongo;

import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.bson.Document;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import scala.Tuple2;

import java.io.InputStream;
import java.util.*;

public class SparkVideo {

    public static void main(String[] args) throws Exception {
        // 读取外部的配置文件。
        InputStream inputStream = SparkVideo.class.getClassLoader().getResourceAsStream("app.properties");
        Properties properties = new Properties();
        properties.load(inputStream);

        // 构建 Spark 配置。
        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkVideo")
                .setMaster("local[*]")
                .set("spark.mongodb.input.uri", properties.getProperty("spark.video.mongodb.input.uri"));

        // 构建 Spark 上下文。
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);

        // 加载 MongoDB 中的数据。
        JavaMongoRDD<Document> rdd = MongoSpark.load(jsc);

//        rdd.foreach(document -> System.out.println(document.toJson()));

        //在数据中有同一个用户对不同的小视频进行评价，需要进行合并操作
        JavaRDD<Document> values = rdd.mapToPair(document -> {
            Integer user = document.getLong("userId").intValue();
            Integer product = document.getLong("videoId").intValue();
            return new Tuple2<>(user + "_" + product, document);
        }).reduceByKey((v1, v2) -> {
            Double score = v1.getDouble("score") + v2.getDouble("score");
            v1.put("score", score);
            return v1;
        }).values();

        // 得到数据中的用户 id 集合。
        List<Long> userIdList = rdd.map(v1 -> v1.getLong("userId")).distinct().collect();

//        values.foreach(document -> System.out.println(document.toJson()));

        // 按照日期对 10 进行取模作为 key，Rating 对象作为 value，获取到数据用于后续的数据处理。
        JavaPairRDD<Long, Rating> ratings = values.mapToPair(document -> {
            Integer user = document.getLong("userId").intValue();
            Integer product = document.getLong("videoId").intValue();
            Double score = document.getDouble("score");
            Long date = document.getLong("date");
            Rating rating = new Rating(user, product, score);
            return new Tuple2<>(date % 10, rating);
        });

        // 通过 MLlib 模型进行推荐，获取到最优的推荐模型。
        MLlibRecommend mLlibRecommend = new MLlibRecommend();
        MatrixFactorizationModel bestModel = mLlibRecommend.bestModel(ratings);

        // 构建 Redis 环境。
        String redisClusterNodes = properties.getProperty("redis.cluster.nodes");
        String[] redisNodes = redisClusterNodes.split(",");
        Set<HostAndPort> nodes = new HashSet<>();
        for (String redisNode : redisNodes) {
            String[] hostAndPorts = redisNode.split(":");
            nodes.add(new HostAndPort(hostAndPorts[0], Integer.valueOf(hostAndPorts[1])));
        }
        JedisCluster jedisCluster = new JedisCluster(nodes);

        // 分别对每一个用户进行推荐，推荐 20 个小视频信息。
        for (Long userId : userIdList) {
            Rating[] recommendProducts = bestModel.recommendProducts(userId.intValue(), 20);

            List<Integer> products = new ArrayList<>();
            for (Rating rating : recommendProducts) {
                products.add(rating.product());
            }

            // 存储到 redis。
            String key = "QUANZI_VIDEO_RECOMMEND_" + userId;
            jedisCluster.set(key, StringUtils.join(products, ','));
        }

        // 关闭连接。
        jedisCluster.close();
        jedisCluster.close();
    }

}
