#!/bin/bash
basedir=`dirname $0`

pushd $basedir > /dev/null

echo "  - importing historical Posts, this may take a while"
echo
java -jar projects/stackexchange/importer/target/stackexchange-importer-full.jar $(pwd)/projects/stackexchange/Posts.xml > /dev/null

echo "  - importing historical Posts, this may take a while"
echo
java -jar projects/stackexchange/importer/target/stackexchange-importer-full.jar $(pwd)/projects/stackexchange/Users.xml > /dev/null

SPARK_HOME=$(cd target/spark-* && pwd)
echo "  - Submitting analytics job to spark master"
echo
$SPARK_HOME/bin/spark-submit --master spark://127.0.0.1:7077 --class org.jboss.datagrid.demo.stackexchange.RunAnalytics projects/stackexchange/spark-analytics/target/stackexchange-spark-analytics-full.jar "spark://127.0.0.1:7077" "127.0.0.1:11322;127.0.0.1:11422;127.0.0.1:11522"

popd > /dev/null
