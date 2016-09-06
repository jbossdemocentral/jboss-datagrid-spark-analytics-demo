package org.jboss.datagrid.demo.rest.model;

/**
 * Created by tqvarnst on 05/09/16.
 */
public class LocationCount {

    String location;
    Long numOfPosts;

    public LocationCount() {
    }

    public LocationCount(String location, Long numOfPosts) {
        this.location = location;
        this.numOfPosts = numOfPosts;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getNumOfPosts() {
        return numOfPosts;
    }

    public void setNumOfPosts(Long numOfPosts) {
        this.numOfPosts = numOfPosts;
    }
}
