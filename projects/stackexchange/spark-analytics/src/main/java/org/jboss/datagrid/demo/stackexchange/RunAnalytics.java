package org.jboss.datagrid.demo.stackexchange;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.infinispan.spark.rdd.InfinispanJavaRDD;
import org.jboss.datagrid.demo.stackexchange.model.Post;
import org.jboss.datagrid.demo.stackexchange.model.User;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

import scala.Tuple2;

/**
 * Created by tqvarnst on 22/08/16.
 */
public class RunAnalytics implements Serializable {
    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern CSV = Pattern.compile(",");
    public static final String DEFAULT_SPARK_MASTER = "spark://localhost:7077";
    public static final String DEFAULT_HOTROD_SERVER_LIST = "localhost:11222";
    public static final String SPARK_APP_NAME = "Java-App";



    private final String postCacheName = "PostStore";
    private final String userCacheName = "UserStore";
    private final String analyticsStore = "AnalyticsStore";

//    private static final String _KEYWORDS = "JAVA,C#,C++,PYTHON,BASH,SCALA";
    private static final String _KEYWORDS = "ALE,LAGER,STOUT";

    private static final List<String> keywordList;

    static {
        keywordList = Arrays.asList(CSV.split(_KEYWORDS));
    }

    private String sparkMasterURL;
    private String hotrodServerList;

    public RunAnalytics(String sparkMasterURL, String hotrodServerList) {
        this.sparkMasterURL = sparkMasterURL;
        this.hotrodServerList = hotrodServerList;

    }

    public void run() {

//        this.queryUsersWithHighestReputation();
        this.mapKeywordCount();

    }

//    private void queryUsersWithHighestReputation() {
//
//        JavaPairRDD<Integer, User> userRDD = getUserRDD();
//        sqlContext = new SQLContext(jsc);
//        DataFrame dataFrame = sqlContext.createDataFrame(userRDD.values(), User.class);
//        dataFrame.registerTempTable("users");
//
//        List<Row> rows = sqlContext.sql("SELECT * FROM users ORDER BY reputation DESC LIMIT 50").collectAsList();
//
//
//        rows.forEach( (row) -> {
//            System.out.println("Result: " + row.mkString(","));
//        });
//
////
////
////        JavaPairRDD<Row, Long> pairRDD = jsc.parallelize(rows).zipWithIndex();
////
////
////        Map<Row, String> collect = rows.stream().collect(Collectors.toMap(Function.identity(), r -> r.mkString(";")));
////
////        collect.entrySet().stream().collect(Collectors.toList(s -> {
////            return new Tuple2<Integer,String>();
////        }));
////
////
////        properties.put("infinispan.rdd.cacheName",analyticsStore);
////
////
////        InfinispanJavaRDD.write(userswithhighestreputation,properties)
//
//
//
//
//
//
////        properties.put("infinispan.rdd.cacheName",postCacheName);
//
//
//
//    }



    private void mapKeywordCount() {

        JavaPairRDD<Integer, Post> postRDD = getPostRDD();


        //Map to words
        JavaRDD<String> words = postRDD.flatMap(new FlatMapFunction<Tuple2<Integer, Post>, String>() {

            @Override
            public Iterable<String> call(Tuple2<Integer, Post> t) throws Exception {
                if (t != null && t._2 != null && t._2.getBody() != null) {
                    List<String> wordList = Arrays.asList(SPACE.split(t._2.getBody()));
                    return wordList;
                }
                return Arrays.asList(new String[]{});
            }
        });

        JavaRDD<String> filteredWords = words.filter(s -> {
            return keywordList.contains(s.toUpperCase());
        });

        //Map to pairs
        JavaPairRDD<String, Integer> ones = filteredWords.mapToPair(
                new PairFunction<String, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(String s) {
                        return new Tuple2<>(s.toUpperCase(), 1);
                    }
                });

        JavaPairRDD<String, Integer> counts = ones.reduceByKey(
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer i1, Integer i2) throws Exception {
                        return i1 + i2;
                    }
                }
        );

        List<Tuple2<String, Integer>> output = counts.collect();



        for (Tuple2<?,?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }

        Properties properties = getHotRodSettings(analyticsStore);
        InfinispanJavaRDD.write(counts,properties);


//        sqlContext = new SQLContext(jsc);
//        DataFrame dataFrame = sqlContext.createDataFrame(postRDD.values(), User.class);
//        System.out.println("RDD size: " + postRDD.values().count());
//        dataFrame.registerTempTable("users");

//        List<Row> rows = sqlContext.sql("SELECT * FROM users ORDER BY reputation DESC LIMIT 10").collectAsList();
//
//
//        JavaPairRDD<Row, Long> pairRDD = jsc.parallelize(rows).zipWithIndex();
//
//
//        Map<Row, String> collect = rows.stream().collect(Collectors.toMap(Function.identity(), r -> r.mkString(";")));
//
//        collect.entrySet().stream().collect(Collectors.toList(s -> {
//            return new Tuple2<Integer,String>();
//        }));
//
//
//        properties.put("infinispan.rdd.cacheName",analyticsStore);
//
//
//        InfinispanJavaRDD.write(userswithhighestreputation,properties)
//        properties.put("infinispan.rdd.cacheName",postCacheName);



    }

    private JavaPairRDD<Integer, Post> getPostRDD() {
        SparkConf sparkConf = new SparkConf().setAppName(SPARK_APP_NAME).setMaster(sparkMasterURL);
        Properties properties = getHotRodSettings(postCacheName);
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        return InfinispanJavaRDD.createInfinispanRDD(jsc, properties);
    }


    private JavaPairRDD<Integer, User> getUserRDD() {
        SparkConf sparkConf = new SparkConf().setAppName(SPARK_APP_NAME).setMaster(sparkMasterURL);
        Properties properties = getHotRodSettings(userCacheName);
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        return InfinispanJavaRDD.createInfinispanRDD(jsc, properties);
    }


    private Properties getHotRodSettings(String cacheName) {
        Properties properties = new Properties();
        properties.put("infinispan.client.hotrod.server_list", hotrodServerList);
        properties.put("infinispan.rdd.cacheName", cacheName);
        return properties;
    }

    private static void printUsage() {
        System.out.println(RunAnalytics.class.getCanonicalName() + " <spark-sparkMasterURL-url> <hotrod-server-list>");
    }


    public final static void main(String args[]) {

        if(args.length==2) {
            new RunAnalytics(args[0],args[1]).run();
        } else if(args.length==1) {
            new RunAnalytics(args[0],DEFAULT_HOTROD_SERVER_LIST).run();
        } else if(args.length==0) {
            new RunAnalytics(DEFAULT_SPARK_MASTER,DEFAULT_HOTROD_SERVER_LIST).run();
        } else {
            System.err.println("Wrong number of arguments!");
            printUsage();
            System.exit(1);
        }
    }
}
