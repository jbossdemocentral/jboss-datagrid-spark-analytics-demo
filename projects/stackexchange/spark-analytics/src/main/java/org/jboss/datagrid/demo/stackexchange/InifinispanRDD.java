package org.jboss.datagrid.demo.stackexchange;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.infinispan.spark.rdd.InfinispanJavaRDD;
import org.jboss.datagrid.demo.stackexchange.model.Post;

import java.util.List;
import java.util.Properties;

/**
 * Created by tqvarnst on 22/08/16.
 */
public class InifinispanRDD {

    private JavaPairRDD<Integer, Post> postsRDD;
    private final JavaSparkContext jsc;

    public InifinispanRDD() {
        SparkConf conf = new SparkConf().setAppName("example-RDD");
        conf.setMaster("spark://q-one:7077");

        jsc = new JavaSparkContext(conf);
        Properties properties = new Properties();
        properties.put("infinispan.client.hotrod.server_list", "localhost:11222;localhost:11322");
        properties.put("infinispan.rdd.cacheName","PostStore");

        postsRDD = InfinispanJavaRDD.createInfinispanRDD(jsc, properties);
    }

    public void countPosts() {

        // Create a SQLContext, registering the data frame and table
        SQLContext sqlContext = new SQLContext(jsc);
        DataFrame dataFrame = sqlContext.createDataFrame(postsRDD.values(), Post.class);
        dataFrame.registerTempTable("posts");

        List<Row> rows = sqlContext.sql("SELECT score,count(*) from posts GROUP BY score ORDER BY score").collectAsList();
//        List<Row> rows = sqlContext.sql("SELECT count(*) from posts WHERE body LIKE '%Belgian%'").collectAsList();
//        List<Row> rows = sqlContext.sql("SELECT body from posts WHERE score>10").collectAsList();

        rows.forEach( (row) -> {
            System.out.println("Result: " + row.mkString(" "));
        });

    }

    public static final void main(String args[]) {
        new InifinispanRDD().countPosts();
    }
}
