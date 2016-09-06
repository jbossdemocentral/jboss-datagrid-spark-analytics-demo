package org.jboss.datagrid.demo;

import org.infinispan.client.hotrod.ServerStatistics;
import org.jboss.datagrid.demo.internal.ServersRegistry;
import org.jboss.datagrid.demo.internal.VisualizerRemoteCacheManager;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by tqvarnst on 01/09/16.
 */

@Path("/info")
@RequestScoped
public class DataGridInfoService {

    @Inject
    VisualizerRemoteCacheManager cm;

    @Inject
    Logger logger;

    @GET
    @Path("/cachenames")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getCacheNames() {
        ServerStatistics stats = cm.getCache().stats();
        logger.info(">>> Default cache stats <<<< ");
        stats.getStatsMap().entrySet().forEach(e -> logger.info(e.getKey() + "=" + e.getValue()));


        ServersRegistry registry = cm.getRegistry();
        List<String> servers = registry.getServers().stream().map(s -> ((InetSocketAddress) s).getHostName() + ":" + ((InetSocketAddress) s).getPort()).collect(Collectors.toList());
        return servers;
    }





    @GET
    @Path("/jmxtest")
    @Produces(MediaType.APPLICATION_JSON)
    public String test() {
        String host = "localhost";
        JMXConnector jmxConnector = null;
        try {
            int port = 9990;  // management-web port
            String urlString =
                    System.getProperty("jmx.service.url","service:jmx:remote+http://" + host + ":" + port);
            JMXServiceURL serviceURL = new JMXServiceURL(urlString);
            Map<String, Object> env = new HashMap<>();
            env.put(JMXConnector.CREDENTIALS, new String[] { "admin", "admin-123" });

            jmxConnector = JMXConnectorFactory.connect(serviceURL, env);
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

            //Invoke on the WildFly MBean server
            return Integer.toString(connection.getMBeanCount());
        } catch(IOException e) {
            System.out.println("Exception in JMX " + e.getMessage());
            e.printStackTrace(System.out);
        }
        finally {
            if (jmxConnector!=null) {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    //Ignore
                }
            }
        }
        return null;

    }

}
