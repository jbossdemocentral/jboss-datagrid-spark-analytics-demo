package org.jboss.datagrid.demo.stackexchange;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.datagrid.demo.stackexchange.model.Post;
import org.jboss.datagrid.demo.stackexchange.model.User;

import javax.print.attribute.standard.MediaSize;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by tqvarnst on 18/08/16.
 */
public class Main {

    public static final int BUFFER_SIZE = 1000;

    private static long elementCount = 0;

    private static final long startTime = System.nanoTime();

    private static RemoteCacheManager cacheManager;

    private static RemoteCache<String, Object> cache;

    public static void main(String[] args) throws JAXBException, IOException {

        if(args.length==0) {
            System.out.println("ERROR: You must supply a file path");
            printUsage();
        }

        Path filePath = FileSystems.getDefault().getPath(args[0]);

        if(!filePath.toFile().exists() || !filePath.toFile().isFile()) {
            System.out.println("ERROR: The file path supplied does not match any existing files!");
            printUsage();
        }

        String first = Files.lines(filePath).findFirst().get();
        if(first!=null && first.contains("<?xml")) {
            first = Files.lines(filePath).skip(1).findFirst().get();
        }

        if(first.trim().equals("<users>")) {
            System.out.println("Parsing users.");
            userParser(filePath);
        }
        else if(first.trim().equals("<posts>")) {
            System.out.println("Parsing posts.");
            postParser(filePath);
        }
        else {
            System.out.println("Couldn't find parser for " + first);
        }


//        rows.forEach(System.out::println);

    }



    private static void userParser(Path filePath) throws JAXBException, IOException {
        Map<Integer,User> bufferMap = new HashMap<Integer,User>(BUFFER_SIZE);
        JAXBContext jc = JAXBContext.newInstance(User.class);
        Unmarshaller u = jc.createUnmarshaller();


        RemoteCache<Integer, User> cache = getRemoteCacheManager().getCache("UserStore");

        Files.lines(filePath).forEach( (line) -> {
            if(line.trim().startsWith("<row")) {
                try {
                    User user = (User) u.unmarshal(new StringReader(line));
                    bufferMap.put(user.getId(),user);
                    if(bufferMap.size()==BUFFER_SIZE) {
                        cache.putAll(bufferMap);
                        increaseAndPrintCount();
                        bufferMap.clear();
                    }
                } catch (JAXBException e) {
                    // Ignore, but report failed lines
                    System.out.println(e.getMessage() + " : " + line);
                }
            }
        });

        if(!bufferMap.isEmpty()) {
            cache.putAll(bufferMap);
            elementCount += bufferMap.size();
            bufferMap.clear();
        }
        printFinalSummary();

    }


    private static void postParser(Path filePath) throws JAXBException, IOException {
        Map<Integer,Post> bufferMap = new HashMap<Integer,Post>(BUFFER_SIZE);
        JAXBContext jc = JAXBContext.newInstance(Post.class);

        Unmarshaller u = jc.createUnmarshaller();

        RemoteCache<Integer, Post> cache = getRemoteCacheManager().getCache("PostStore");

        Files.lines(filePath).forEach( (line) -> {
            if(line.trim().startsWith("<row")) {
                try {
                    Post post = (Post) u.unmarshal(new StringReader(line));
                    bufferMap.put(post.getId(),post);
                    if(bufferMap.size()==BUFFER_SIZE) {
                        cache.putAll(bufferMap);
                        increaseAndPrintCount();
                        bufferMap.clear();
                    }
                } catch (JAXBException e) {
                    // Ignore failed lines
                    System.out.println(e.getMessage() + " : " + line);
                }
            }
        });
        if(!bufferMap.isEmpty()) {
            cache.putAll(bufferMap);
            elementCount += bufferMap.size();
            bufferMap.clear();
        }
        printFinalSummary();

    }

    private static void printUsage() {
        System.out.println("Usage of the stackexchange importer: ");
        System.out.println("\t java -jar stackexchange-importer-full.jar [file-path]");

    }

    private static RemoteCacheManager getRemoteCacheManager() throws IOException {
        Properties jdgprops = new Properties();
        jdgprops.load(Main.class.getClassLoader().getResourceAsStream("jdg.properties"));
        String serverList = System.getProperty("jdg.visualizer.serverList");
        if(serverList!=null && !serverList.isEmpty()) {
            jdgprops.setProperty("infinispan.client.hotrod.server_list", serverList);
        }


        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.withProperties(jdgprops);

        return new RemoteCacheManager(builder.build());
    }

    private static void increaseAndPrintCount() {
        elementCount += BUFFER_SIZE;
        System.out.println(String.format("Sending %d items to JBoss Data grid for a total of %d",BUFFER_SIZE,elementCount));
    }

    private static void printFinalSummary() {
        final double totalTime = System.nanoTime()-startTime;
        System.out.println(String.format("Totally %d elements where sent the data grid in %.2f seconds.",elementCount,totalTime/SECONDS.toNanos(1)));
    }
}
