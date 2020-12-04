package com.ax.code.es.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

/**
 * @author lj
 */
public class FacetsGet {
    public FacetsGet() {
    }

    public FacetsResult getFacets(SearchResponse res, String key, AggregationBuilder aggsBuilder) {
        new FacetsResult();
        FacetsGet facetsGet = new FacetsGet();
        FacetsResult result;
        if (aggsBuilder.getClass().equals(TermsAggregationBuilder.class)) {
            result = facetsGet.termFacets(res, key);
        } else {
            result = null;
        }

        return result;
    }

    public FacetsResult termFacets(SearchResponse res, String key) {
        List<Map<String, Object>> resultList = new ArrayList();
        FacetsResult result = new FacetsResult();
        Terms terms = (Terms)res.getAggregations().getAsMap().get(key);
        Iterator var6 = terms.getBuckets().iterator();

        while(var6.hasNext()) {
            Terms.Bucket bucket = (Terms.Bucket)var6.next();
            Map<String, Object> resultMap = new HashMap();
            resultMap.put("term", bucket.getKey().toString());
            resultMap.put("count", String.valueOf(bucket.getDocCount()));
            resultList.add(resultMap);
        }

        result.install(resultList, res.getAggregations().getAsMap().size());
        return result;
    }
}
