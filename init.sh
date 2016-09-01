#!/bin/bash
basedir=`dirname $0`


DEMO="JBoss Data Grid and Spark Analytics Demo"
AUTHORS="Thomas Qvarnstrom, Red Hat"
SRC_DIR=$basedir/installs

SPARK_INSTALL=spark-1.6.2-bin-hadoop2.6.tgz
JDG_INSTALL=jboss-datagrid-7.0.0-server.zip
EAP_INSTALL=jboss-eap-7.0.0.zip

SOFTWARE=($SPARK_INSTALL $JDG_INSTALL $EAP_INSTALL)


# wipe screen.
clear

echo

ASCII_WIDTH=52

printf "##  %-${ASCII_WIDTH}s  ##\n" | sed -e 's/ /#/g'
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n" "Setting up the ${DEMO}"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n" "    # ####   ###   ###  ###   ###   ###"
printf "##  %-${ASCII_WIDTH}s  ##\n" "    # #   # #   # #    #      #  # #"
printf "##  %-${ASCII_WIDTH}s  ##\n" "    # ####  #   #  ##   ##    #  # #  ##"
printf "##  %-${ASCII_WIDTH}s  ##\n" "#   # #   # #   #    #    #   #  # #   #"
printf "##  %-${ASCII_WIDTH}s  ##\n" " ###  ####   ###  ###  ###    ###   ###"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n" "brought to you by,"
printf "##  %-${ASCII_WIDTH}s  ##\n" "${AUTHORS}"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n"
printf "##  %-${ASCII_WIDTH}s  ##\n" | sed -e 's/ /#/g'

echo
echo "Setting up the ${DEMO} environment..."
echo


# Check that java is installed and on the path
java -version 2>&1 | grep "java version" | grep 1.8 > /dev/null || { echo >&2 "Java 1.8 is required but not installed... aborting."; exit 1; }

# Check that maven is installed and on the path
mvn -v -q >/dev/null 2>&1 || { echo >&2 "Maven is required but not installed yet... aborting."; exit 1; }

# Verify that necesary files are downloaded
for DONWLOAD in ${SOFTWARE[@]}
do
	if [[ -r $SRC_DIR/$DONWLOAD || -L $SRC_DIR/$DONWLOAD ]]; then
			echo $DONWLOAD are present...
			echo
	else
			echo You need to download $DONWLOAD from the Customer Support Portal
			echo and place it in the $SRC_DIR directory to proceed...
			echo
			exit
	fi
done


echo "  - stopping any running spark slave instances"
echo
jps -lm | grep org.apache.spark.deploy.worker.Worker | grep -v grep | awk '{print $1}' | xargs kill > /dev/null

echo "  - stopping any running spark master instances"
echo
jps -lm | grep org.apache.spark.deploy.master.Master | grep -v grep | awk '{print $1}' | xargs kill > /dev/null

#If JDG is running stop it
echo "  - stopping any running datagrid instances"
jps -lm | grep jboss-datagrid | grep -v grep | awk '{print $1}' | xargs kill  > /dev/null


sleep 2
echo


# Create the target directory if it does not already exist.
if [ -x target ]; then
		echo "  - deleting existing target directory..."
		echo
		rm -rf target
fi
echo "  - creating the target directory..."
echo
mkdir target



# Unzip the maven repo files
echo "  - installing spark"
echo
tar -zxf $SRC_DIR/$SPARK_INSTALL -C target > /dev/null

#unzip -q -d target $SRC_DIR/$FUSE_INSTALL


SPARK_HOME=$(cd target/spark-* && pwd)

echo "  - installing datagrid"
echo
unzip -q -d target $SRC_DIR/$JDG_INSTALL

JDG_HOME=$(cd target/jboss-datagrid-7* && pwd)

echo "  - configuring JBoss Data Grid "
echo
$JDG_HOME/bin/add-user.sh -s -u admin -p admin-123
$JDG_HOME/bin/cli.sh --file=support/datagrid-setup.cli > /dev/null


echo "  - starting JDG"
echo

pushd target/jboss-datagrid-7*/bin > /dev/null
./domain.sh > /dev/null &
popd > /dev/null

echo "  - waiting for all servers to become available"
until $($JDG_HOME/bin/cli.sh -c --command="/host=master/server=server-one:read-attribute(name=server-state)" | grep result | grep running > /dev/null)
do
    sleep 1
    printf "."
done
until $($JDG_HOME/bin/cli.sh -c --command="/host=master/server=server-two:read-attribute(name=server-state)" | grep result | grep running > /dev/null)
do
    sleep 1
    printf "."
done
until $($JDG_HOME/bin/cli.sh -c --command="/host=master/server=server-three:read-attribute(name=server-state)" | grep result | grep running > /dev/null)
do
    sleep 1
    printf "."
done
echo
echo

echo "  - installing JBoss EAP"
echo
unzip -q -d target $SRC_DIR/$EAP_INSTALL

EAP_HOME=$(cd target/jboss-eap-7* && pwd)

echo "  - configuring JBoss EAP"
echo
$EAP_HOME/bin/add-user.sh -s -u admin -p admin-123
cp projects/jdg-visualizer/target/jdg-visualizer.war $EAP_HOME/standalone/deployments

echo "  - starting EAP"
echo

pushd target/jboss-eap-7*/bin > /dev/null
./standalone.sh -Djboss.socket.binding.port-offset=300 -b 0.0.0.0 -bmanagement=0.0.0.0 -Djdg.visualizer.jmxUser=admin -Djdg.visualizer.jmxPass=admin-123 -Djdg.visualizer.serverList=localhost:11222 > /dev/null &
popd > /dev/null

echo "  - starting Spark master on localhost"
echo

pushd target/spark-1.6* > /dev/null
sbin/start-master.sh --webui-port 9080 -h localhost > /dev/null &
popd > /dev/null

echo "  - starting Spark slave localhost"
echo

pushd target/spark-1.6* > /dev/null
sbin/start-slave.sh spark://localhost:7077 > /dev/null &
popd > /dev/null

echo "  - building the stackexchange project"
echo
pushd projects/stackexchange > /dev/null
mvn -q package
popd > /dev/null

echo "  - importing historical Posts, this may take a while"
echo
java -jar projects/stackexchange/importer/target/stackexchange-importer-full.jar $(pwd)/projects/stackexchange/Posts.xml > /dev/null

echo "  - importing historical Posts, this may take a while"
echo
java -jar projects/stackexchange/importer/target/stackexchange-importer-full.jar $(pwd)/projects/stackexchange/Users.xml > /dev/null

echo "  - Submitting analytics job to spark master"
echo
$SPARK_HOME/bin/spark-submit --master spark://127.0.0.1:7077 --class org.jboss.datagrid.demo.stackexchange.RunAnalytics projects/stackexchange/spark-analytics/target/stackexchange-spark-analytics-full.jar
