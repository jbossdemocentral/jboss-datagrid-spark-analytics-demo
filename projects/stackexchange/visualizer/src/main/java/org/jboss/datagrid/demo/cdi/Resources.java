/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.datagrid.demo.cdi;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.datagrid.demo.internal.VisualizerRemoteCacheManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.logging.Logger;

/**
 * This class uses CDI to alias Java EE resources
 *
 * 
 * @author <a href="mailto:tqvarnst@redhat.com">Thomas Qvarnstrom</a>
 */
public class Resources {
    private String refreshRate = System.getProperty("jdg.visualizer.refreshRate", "2000");
	private String jmxUsername = System.getProperty("jdg.visualizer.jmxUser", "admin");
	private String jmxPassword = System.getProperty("jdg.visualizer.jmxPass", "jboss");

	private int jmxHotrodPortOffset = Integer.parseInt(System.getProperty("jdg.visualizer.jmxPortOffset", "1223"));

	private String nodeColorAsString = System.getProperty("jdg.visualizer.nodeColor");
	
	@Produces
	public Logger produceLog(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
	}

//	@Produces
//	@Default
//	@ApplicationScoped
//	public RemoteCacheManager cacheManager() {
//        ConfigurationBuilder builder = new ConfigurationBuilder();
//        builder.addServer()
//                .host("localhost")
//                .port(11222);
//        RemoteCacheManager cm = new RemoteCacheManager(builder.build());
//        cm.start();
//        return cm;
//	}

	@Produces
	@Default
	@ApplicationScoped
	public VisualizerRemoteCacheManager cacheManager() {
		VisualizerRemoteCacheManager cm = new VisualizerRemoteCacheManager();
		cm.start();
		return cm;
	}



	public void destroyCacheManager(@Disposes RemoteCacheManager cm) {
		cm.stop();
	}
}
