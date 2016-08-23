JBoss Data Grid Spark Analytics Demo
=====================================

This demo shows how to JBoss Data Grid integration with Spark is work. It also uses Zeppelin to show nice reports.

Setup
---------------
To setup the infrastructure for the demo download the follwoing files to the `installs` directory:

* [jboss-datagrid-7.0.0-server.zip](https://developers.redhat.com/download-manager/file/jboss-datagrid-7.0.0-server.zip)
* [spark-1.6.2-bin-hadoop2.6.tgz](http://www.apache.org/dyn/closer.lua/spark/spark-1.6.2/spark-1.6.2-bin-hadoop2.6.tgz)
* [zeppelin-0.6.0-bin-all.tgz](http://archive.apache.org/dist/zeppelin/zeppelin-0.6.0/zeppelin-0.6.0-bin-all.tgz)

After that run the `init.sh` script

		$ sh init.sh

Run the Demo
---------------

After that open the zeppelin [web-console](http://localhost:8080) and import notebook `support/demo-notebook.json`
