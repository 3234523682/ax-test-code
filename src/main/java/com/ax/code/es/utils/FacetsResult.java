package com.ax.code.es.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author lj
 */
public class FacetsResult implements Serializable {
    private int totalHits;
    private List<Map<String, Object>> results;

    public FacetsResult() {
    }

    public void install(List<Map<String, Object>> result, int totalHits) {
        this.results = result;
        this.totalHits = totalHits;
    }

    public int getTotalHits() {
        if (this.totalHits < 0) {
            this.totalHits = 0;
        }

        return this.totalHits;
    }

    public void setTotalHits(int totalHits) {
        this.totalHits = totalHits;
    }

    public List<Map<String, Object>> getResults() {
        return this.results;
    }

    public void setResults(List<Map<String, Object>> results) {
        this.results = results;
    }
}
