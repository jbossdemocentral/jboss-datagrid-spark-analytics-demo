package org.jboss.datagrid.demo.stackexchange.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlRootElement(name="row")
@XmlAccessorType(XmlAccessType.FIELD)
public class User implements Serializable {
    @XmlAttribute(name = "Id")
    int id;

    @XmlAttribute(name = "Reputation")
    int reputation;

    @XmlAttribute(name = "CreateDate")
    String creationDate;

    @XmlAttribute(name = "DisplayName")
    String displayName;

    @XmlAttribute(name = "LastAccessDate")
    String lastAccessDate;

    @XmlAttribute(name = "WebsiteUrl")
    String websiteUrl;

    @XmlAttribute(name = "Location")
    String location;

    @XmlAttribute(name = "AboutMe")
    String aboutMe;

    @XmlAttribute(name = "Views")
    int views;

    @XmlAttribute(name = "UpVotes")
    String upVotes;

    @XmlAttribute(name = "DownVotes")
    String downVotes;

    @XmlAttribute(name = "Age")
    int age;

    @XmlAttribute(name = "AccountId")
    String accountId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(String lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getUpVotes() {
        return upVotes;
    }

    public void setUpVotes(String upVotes) {
        this.upVotes = upVotes;
    }

    public String getDownVotes() {
        return downVotes;
    }

    public void setDownVotes(String downVotes) {
        this.downVotes = downVotes;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return String.format("User id: %d, displayName: %s",this.id,this.displayName);
    }
}