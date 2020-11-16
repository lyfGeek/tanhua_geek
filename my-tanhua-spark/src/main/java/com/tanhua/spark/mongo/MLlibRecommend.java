package com.tanhua.spark.mongo;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import scala.Tuple2;

public class MLlibRecommend {

    public MatrixFactorizationModel bestModel(JavaPairRDD<Long, Rating> ratings) {
        // 统计有用户数量和动态数量以及用户对动态的评分数目。
        Long numRatings = ratings.count();
        Long numUsers = ratings.map(v1 -> (v1._2()).user()).distinct().count();
        Long numMovies = ratings.map(v1 -> (v1._2()).product()).distinct().count();
        System.out.println("用户：" + numUsers + "动态：" + numMovies + "评论：" + numRatings);

        // 将样本评分表以 key 值切分成 3 个部分，分别用于训练（60%，并加入用户评分），校验（20%）, and 测试（20%）。
        // 该数据在计算过程中要多次应用到，所以 cache 到内存。

        Integer numPartitions = 4;// 分区数。
        // 训练集。
        JavaRDD<Rating> training = ratings
                .filter(v -> v._1() < 6)
                .values()
                .repartition(numPartitions)
                .cache();

        // 校验集。
        JavaRDD<Rating> validation = ratings
                .filter(v -> v._1() >= 6 && v._1() < 8)
                .values()
                .repartition(numPartitions).cache();

        // 测试集。
        JavaRDD<Rating> test = ratings
                .filter(v -> v._1() >= 8)
                .values()
                .cache();

        Long numTraining = training.count();
        Long numValidation = validation.count();
        Long numTest = test.count();
        System.out.println("训练集：" + numTraining + " 校验集：" + numValidation + " 测试集：" + numTest);

        // 训练不同参数下的模型，并在校验集中验证，获取最佳参数下的模。
        int[] ranks = new int[]{10, 11, 12};
//        double[] lambdas = new double[]{0.01, 0.03, 0.1, 0.3, 1, 3};
        double[] lambdas = new double[]{0.01};
//        int[] numIters = new int[]{8, 9, 10, 11, 12, 13, 14, 15};
        int[] numIters = new int[]{8, 9, 10};

        MatrixFactorizationModel bestModel = null;
        double bestValidationRmse = Double.MAX_VALUE;
        int bestRank = 0;
        double bestLambda = -0.01;
        int bestNumIter = 0;

        for (int rank : ranks) {
            for (int numIter : numIters) {
                for (double lambda : lambdas) {
                    MatrixFactorizationModel model = ALS.train(training.rdd(), rank, numIter, lambda);
                    Double validationRmse = computeRmse(model, validation, numValidation);
                    System.out.println("RMSE（校验集）= " + validationRmse + ", rank = " + rank + ", lambda = " + lambda + ", numIter = " + numIter);

                    if (validationRmse < bestValidationRmse) {
                        bestModel = model;
                        bestValidationRmse = validationRmse;
                        bestRank = rank;
                        bestLambda = lambda;
                        bestNumIter = numIter;
                    }
                }
            }
        }

        double testRmse = computeRmse(bestModel, test, numTest);
        System.out.println("测试数据集在 最佳训练模型 rank = " + bestRank + ", lambda = " + bestLambda + ", numIter = " + bestNumIter + ", RMSE = " + testRmse);

        // 计算均值。
        Double meanRating = training.union(validation).mapToDouble(v -> v.rating()).mean();

        // 计算标准误差值。
        Double baselineRmse = Math.sqrt(test.map(v -> (meanRating - v.rating()) * (meanRating - v.rating())).reduce((v1, v2) -> (v1 + v2) / numTest));

        // 计算准确率提升了多少。
        double improvement = (baselineRmse - testRmse) / baselineRmse * 100;

        System.out.println("最佳训练模型的准确率提升了：" + String.format("%.2f", improvement) + "%.");

        // 构建最佳训练模型。
        bestModel = ALS.train(ratings.values().rdd(), bestRank, bestNumIter, bestLambda);

        return bestModel;
    }

    /**
     * 校验集预测数据和实际数据之间的均方根误差。
     *
     * @param model
     * @param data
     * @param n
     * @return
     */
    public Double computeRmse(MatrixFactorizationModel model, JavaRDD<Rating> data, Long n) {
        // 进行预测。
        JavaRDD<Rating> predictions = model.predict(data.mapToPair(v -> new Tuple2<>(v.user(), v.product())));

        JavaRDD<Tuple2<Double, Double>> predictionsAndRatings = predictions
                .mapToPair(v -> new Tuple2<>(new Tuple2<>(v.user(), v.product()), v.rating()))
                .join(data.mapToPair(v -> new Tuple2<>(new Tuple2<>(v.user(), v.product()), v.rating()))).values();

        Double reduce = predictionsAndRatings.map(v -> (v._1 - v._2) * (v._1 - v._2))
                .reduce((v1, v2) -> (v1 + v2) / n);
        // 正平方根。
        return Math.sqrt(reduce);
    }

}
