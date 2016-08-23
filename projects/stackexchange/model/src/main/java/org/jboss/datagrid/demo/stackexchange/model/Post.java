package org.jboss.datagrid.demo.stackexchange.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="row")
@XmlAccessorType(XmlAccessType.FIELD)
public class Post implements Serializable {
    @XmlAttribute(name = "Id")
    int id;

    @XmlAttribute(name = "PostTypeId")
    int postTypeId;

    @XmlAttribute(name = "CreateDate")
    String creationDate;

    @XmlAttribute(name = "Title")
    String title;

    @XmlAttribute(name = "Body")
    String body;

    @XmlAttribute(name = "Tags")
    String tags;

    @XmlAttribute(name = "Score")
    int score;

    @XmlAttribute(name = "OwnerUserId")
    int ownerUserId;

    @XmlAttribute(name = "CommentCount")
    int commentCount;

    public Post() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostTypeId() {
        return postTypeId;
    }

    public void setPostTypeId(int postTypeId) {
        this.postTypeId = postTypeId;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public String toString() {
        return String.format("Post id: %d, Title: %s",this.id,this.title);
    }
}