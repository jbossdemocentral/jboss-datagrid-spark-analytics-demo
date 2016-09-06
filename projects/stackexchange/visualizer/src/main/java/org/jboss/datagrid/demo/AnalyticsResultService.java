package org.jboss.datagrid.demo;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.jboss.datagrid.demo.rest.model.LocationCount;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.Location;

/**
 * Created by tqvarnst on 01/09/16.
 */

@Path("/results")
@RequestScoped
public class AnalyticsResultService {

    public static final int MAX_SIZE_USER_RANKING = 5;
    public static final int MAX_SIZE_KEYWORD_RANKING = 5;
    public static final int MAX_SIZE_LOCATIONS = 3;

    private final String usersWithHighestReputationStore = "HighestRankedAnalyticsStore";
    private final String keywordAnalyticsStore = "KeyWordAnalyticsStore";
    private final String locationAnalyticsStore = "LocationAnalyticsStore";




    @Inject
    RemoteCacheManager cm;

    @GET
    @Path("/keywordcount")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,Integer> getKeywordCount() {
        RemoteCache<String, Integer> cache = cm.getCache(keywordAnalyticsStore);

        return cache.getAll(cache.keySet()).entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(MAX_SIZE_KEYWORD_RANKING)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                LinkedHashMap::new));

    }

    @GET
    @Path("/userranking")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,Integer> getUserRankingCount() {
        RemoteCache<String, Integer> cache = cm.getCache(usersWithHighestReputationStore);

        return cache.getAll(cache.keySet()).entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(MAX_SIZE_USER_RANKING)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

    }

    @GET
    @Path("/locations")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LocationCount> getLocations() {
        RemoteCache<String, Long> cache = cm.getCache(locationAnalyticsStore);
        System.out.println("Size of cache is " + cache.size());

        cache.keySet().forEach(System.out::println);

        return cache.getAll(cache.keySet()).entrySet()
                .stream()
                .map(m -> new LocationCount(m.getKey(),m.getValue()))
//                .map(new Function<Map.Entry<String,Long>, LocationCount>() {
//                    @Override
//                    public LocationCount apply(Map.Entry<String, Long> m) {
//                        return new LocationCount(m.getKey(),m.getValue());
//                    }
//                })
                .limit(MAX_SIZE_LOCATIONS)
                .collect(Collectors.toList());


    }
}
