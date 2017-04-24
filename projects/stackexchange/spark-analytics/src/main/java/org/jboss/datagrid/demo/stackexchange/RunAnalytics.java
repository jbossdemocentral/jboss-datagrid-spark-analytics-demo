package org.jboss.datagrid.demo.stackexchange;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
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
    public static final String DEFAULT_HOTROD_SERVER_LIST = "localhost:11322";
    public static final String SPARK_APP_NAME = "JDG-Spark-App";



    private final String postCacheName = "PostStore";
    private final String userCacheName = "UserStore";
    private final String analyticsStore = "KeyWordAnalyticsStore";
    private final String locationAnalyticsStore = "LocationAnalyticsStore";
    private final String usersWithHighestReputationStore = "HighestRankedAnalyticsStore";

//    private static final String _KEYWORDS = "JAVA,C#,C++,PYTHON,BASH,SCALA";
    private static final String _KEYWORDS = "ALE,LAGER,STOUT";

    private static final List<String> keywordList;

    static {
        keywordList = Arrays.asList(CSV.split(_KEYWORDS));
    }

    private static String sparkMasterURL;
    private String hotrodServerList;
    private static JavaSparkContext jsc;

    public RunAnalytics(String sparkMasterURL, String hotrodServerList) {
        RunAnalytics.sparkMasterURL = sparkMasterURL;
        this.hotrodServerList = hotrodServerList;

    }

    public void run() {

        this.queryUsersWithHighestReputation();
        this.keywordCount();
        this.locationOfPosts();

    }

    private void queryUsersWithHighestReputation() {

        JavaPairRDD<Integer, User> userRDD = getUserRDD();
        SQLContext sqlContext = new SQLContext(RunAnalytics.getJavaSparkContext());
        DataFrame dataFrame = sqlContext.createDataFrame(userRDD.values(), User.class);
        dataFrame.registerTempTable("users");

        List<Row> rows = sqlContext.sql("SELECT displayName,reputation FROM users ORDER BY reputation DESC LIMIT 10").collectAsList();


        rows.forEach( (row) -> {
            System.out.println("Result: " + row.mkString(","));
        });

        JavaPairRDD<String, Integer> rankedUsersRDD = RunAnalytics.getJavaSparkContext().parallelize(rows).mapPartitionsToPair(
            new PairFlatMapFunction<Iterator<Row>, String, Integer>() {
                @Override
                public Iterable<Tuple2<String, Integer>> call(Iterator<Row> rowIterator) throws Exception {
                    List<Tuple2<String,Integer>> list = new ArrayList<>();
                    while(rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        list.add(new Tuple2<>(row.getString(0),row.getInt(1)));
                    }
                    return list;
                }
            });


        Properties properties = getHotRodSettings(usersWithHighestReputationStore);
        InfinispanJavaRDD.write(rankedUsersRDD,properties);
    }



    private void keywordCount() {

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



        Properties properties = getHotRodSettings(analyticsStore);
        InfinispanJavaRDD.write(counts,properties);

    }

    private void locationOfPosts() {
        System.out.println(">>> RUNNING LOCATION QUERY");
        JavaPairRDD<Integer, User> userRDD = getUserRDD();
        JavaPairRDD<Integer, Post> postRDD = getPostRDD();

        SQLContext sqlContext = new SQLContext(RunAnalytics.getJavaSparkContext());

        DataFrame dataFrame = sqlContext.createDataFrame(userRDD.values(), User.class);
        dataFrame.registerTempTable("users");

        DataFrame dataFrame2 = sqlContext.createDataFrame(postRDD.values(), Post.class);
        dataFrame2.registerTempTable("posts");

        List<Row> rows = sqlContext.sql("SELECT u.location as `Location`,count(p.id) as `Posts` FROM users u INNER JOIN posts p  ON u.id=p.ownerUserId WHERE p.postTypeId=1 GROUP BY u.location ORDER BY `Posts` DESC LIMIT 10").collectAsList();

        System.out.println(">>> LOCATION QUERY RETURNED " + rows.size() + " RESULTS");

        rows.forEach( (row) -> {
            System.out.println("Result: " + row.mkString(","));
        });

        JavaPairRDD<String, Long> locationsRDD = RunAnalytics.getJavaSparkContext().parallelize(rows).mapPartitionsToPair(
                new PairFlatMapFunction<Iterator<Row>, String, Long>() {
                    @Override
                    public Iterable<Tuple2<String, Long>> call(Iterator<Row> rowIterator) throws Exception {
                        List<Tuple2<String,Long>> list = new ArrayList<>();
                        while(rowIterator.hasNext()) {
                            Row row = rowIterator.next();
                            String location = row.getString(0);
                            long count = row.getLong(1);
                            location = (location==null?"Undefined":location);
                            list.add(new Tuple2<>(location, count));
                        }
                        return list;
                    }
                });


        Properties properties = getHotRodSettings(locationAnalyticsStore);
        InfinispanJavaRDD.write(locationsRDD,properties);

    }


    private JavaPairRDD<Integer, Post> getPostRDD() {
        JavaSparkContext jsc = getJavaSparkContext();
        Properties properties = getHotRodSettings(postCacheName);
        return InfinispanJavaRDD.createInfinispanRDD(jsc, properties);
    }




    private JavaPairRDD<Integer, User> getUserRDD() {
        JavaSparkContext jsc = getJavaSparkContext();
        Properties properties = getHotRodSettings(userCacheName);
        return InfinispanJavaRDD.createInfinispanRDD(jsc, properties);
    }


    private static JavaSparkContext getJavaSparkContext() {
        if (jsc==null) {
            SparkConf sparkConf = new SparkConf().setAppName(SPARK_APP_NAME).setMaster(sparkMasterURL);
            jsc = new JavaSparkContext(sparkConf);
        }
        return jsc;
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
