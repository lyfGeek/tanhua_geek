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

public class SparkQunaZi {

    public static void main(String[] args) throws Exception {
        // 加载外部的配置文件 app.properties。
        InputStream inputStream = SparkQunaZi.class.getClassLoader().getResourceAsStream("app.properties");
        Properties properties = new Properties();
        properties.load(inputStream);

        // 构建 Spark 配置。
        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkQunaZi")
                .setMaster("local[*]")
                .set("spark.mongodb.input.uri", properties.getProperty("spark.mongodb.input.uri"));

        // 构建 Spark 上下文。
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);

        // 加载 MongoDB 中的数据。
        JavaMongoRDD<Document> rdd = MongoSpark.load(jsc);

        // 打印测试数据。
//        rdd.foreach(document -> System.out.println(document.toJson()));

        // 在数据中会存在，同一个用户对不同的动态（相同动态）进行操作，需要合并操作。
        JavaRDD<Document> values = rdd.mapToPair(document -> {
            Long userId = document.getLong("userId");
            Long publishId = document.getLong("publishId");
            return new Tuple2<>(userId + "_" + publishId, document);
        }).reduceByKey((v1, v2) -> {
            double newScore = v1.getDouble("score") + v2.getDouble("score");
            v1.put("score", newScore);
            return v1;
        }).values();

        // 用户列表。
        List<Long> userIdList = rdd.map(v1 -> v1.getLong("userId")).distinct().collect();

        // 数据的打印，测试。
//        values.foreach(document -> System.out.println(document.toJson()));

        JavaPairRDD<Long, Rating> ratings = values.mapToPair(document -> {
            Long date = document.getLong("date");
            int userId = document.getLong("userId").intValue();
            int publishId = document.getLong("publishId").intValue();
            Double score = document.getDouble("score");
            Rating rating = new Rating(userId, publishId, score);
            return new Tuple2<>(date % 10, rating);
        });

        MLlibRecommend mLlibRecommend = new MLlibRecommend();
        MatrixFactorizationModel bestModel = mLlibRecommend.bestModel(ratings);

        // 连接 redis，做存储。
        String redisNodesStr = properties.getProperty("redis.cluster.nodes");
        String[] redisNodesStrs = StringUtils.split(redisNodesStr, ',');
        Set<HostAndPort> nodes = new HashSet<>();
        for (String nodesStr : redisNodesStrs) {
            String[] ss = StringUtils.split(nodesStr, ':');
            nodes.add(new HostAndPort(ss[0], Integer.valueOf(ss[1])));
        }
        JedisCluster jedisCluster = new JedisCluster(nodes);

        for (Long userId : userIdList) {
            Rating[] recommendProducts = bestModel.recommendProducts(userId.intValue(), 20);

            List<Integer> products = new ArrayList<>();

            for (Rating product : recommendProducts) {
                products.add(product.product());
            }

            String key = "QUANZI_PUBLISH_RECOMMEND_" + userId;
            jedisCluster.set(key, StringUtils.join(products, ','));
        }

        // 关闭。
        jedisCluster.close();
        jsc.close();
    }

}
