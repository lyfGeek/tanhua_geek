package com.tanhua.spark.mongo;

import com.mongodb.spark.MongoSpark;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import scala.Tuple2;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class SparkUserRecommend {

    public static void main(String[] args) throws Exception {
        // 加载外部的配置文件 app.properties。
        InputStream inputStream = SparkQunaZi.class.getClassLoader().getResourceAsStream("app.properties");
        Properties properties = new Properties();
        properties.load(inputStream);

        // 构建 Spark 配置。
        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkUserRecommend")
                .setMaster("local[*]")
                .set("spark.mongodb.output.uri", properties.getProperty("spark.mongodb.output.user.uri"));

        // 加载 mysql 数据。
        SparkSession sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
        String url = properties.getProperty("jdbc.url");

        // 设置数据库连接信息。
        Properties connectionProperties = new Properties();
        connectionProperties.put("driver", properties.getProperty("jdbc.driver-class-name"));
        connectionProperties.put("user", properties.getProperty("jdbc.username"));
        connectionProperties.put("password", properties.getProperty("jdbc.password"));

        JavaRDD<Row> userInfoRdd = sparkSession.read().jdbc(url, "tb_user_info", connectionProperties).toJavaRDD();

        // 用户列表。
        List<Long> userIds = userInfoRdd.map(v -> v.getLong(1)).collect();

        // 计算出这张表数据的笛卡尔积。
        JavaPairRDD<Row, Row> cartesian = userInfoRdd.cartesian(userInfoRdd);

        // 计算用户的相似度。
        JavaPairRDD<Long, Rating> javaPairRDD = cartesian.mapToPair(row -> {
            Row row1 = row._1();
            Row row2 = row._2();

            Long userId1 = row1.getLong(1);
            Long userId2 = row2.getLong(1);

            Long key = userId1 + userId2 + RandomUtils.nextLong();

            // 自己与自己对比。
            if (userId1.longValue() == userId2.longValue()) {
                return new Tuple2<>(key % 10, new Rating(userId1.intValue(), userId2.intValue(), 0));
            }

            double score = 0;

            // 计算年龄差。
            int ageDiff = Math.abs(row1.getInt(6) - row2.getInt(6));
            if (ageDiff <= 2) {
                score += 30;
            } else if (ageDiff >= 3 && ageDiff <= 5) {
                score += 20;
            } else if (ageDiff > 5 && ageDiff <= 10) {
                score += 10;
            }

            // 计算性别。
            if (row1.getInt(5) != row2.getInt(5)) {
                score += 30;
            }

            // 计算城市。
            String city1 = StringUtils.substringBefore(row1.getString(8), "-");
            String city2 = StringUtils.substringBefore(row2.getString(8), "-");
            if (StringUtils.equals(city1, city2)) {
                score += 20;
            }

            // 计算学历。
            String edu1 = row1.getString(7);
            String edu2 = row2.getString(7);
            if (StringUtils.equals(edu1, edu2)) {
                score += 20;
            }

            Rating rating = new Rating(userId1.intValue(), userId2.intValue(), score);
            return new Tuple2<>(key % 10, rating);

        });

        // MLlib 进行计算最佳的推荐模型。
        MLlibRecommend mLlibRecommend = new MLlibRecommend();
        MatrixFactorizationModel bestModel = mLlibRecommend.bestModel(javaPairRDD);

        // 将数据写入到 MongoDB 中。
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkSession.sparkContext());

        for (Long userId : userIds) {
            Rating[] ratings = bestModel.recommendProducts(userId.intValue(), 50);

            JavaRDD<Document> documentJavaRDD = javaSparkContext.parallelize(Arrays.asList(ratings)).map(v1 -> {
                Document document = new Document();

                document.put("_id", ObjectId.get());
                document.put("userId", v1.product());
                document.put("toUserId", v1.user());
                // 得分，保留 2 位小数.
                double score = new BigDecimal(v1.rating()).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
                document.put("score", score);
                document.put("date", new DateTime().toString("yyyy/MM/dd"));

                return document;
            });

            MongoSpark.save(documentJavaRDD);
        }
    }

}
