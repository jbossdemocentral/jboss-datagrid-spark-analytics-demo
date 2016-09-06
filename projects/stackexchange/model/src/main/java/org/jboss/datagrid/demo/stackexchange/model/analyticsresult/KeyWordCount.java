package org.jboss.datagrid.demo.stackexchange.model.analyticsresult;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by tqvarnst on 05/09/16.
 */
public class KeyWordCount implements Serializable{

    private Date reportDate;

    private Map<String,Integer> keywords;

    public KeyWordCount() {
    }

    public KeyWordCount(Date reportDate, Map<String, Integer> keywords) {
        this.reportDate = reportDate;
        this.keywords = keywords;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public Map<String, Integer> getKeywords() {
        return keywords;
    }

    public void setKeywords(Map<String, Integer> keywords) {
        this.keywords = keywords;
    }
}
