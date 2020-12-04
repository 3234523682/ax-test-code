package com.ax.code.es.esservice;

/**
 * @author lj
 */

import com.ax.code.es.utils.FacetsResult;
import com.ax.code.es.page.Page;
import com.ax.code.es.page.Pageable;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;

public interface EsService<T> {
    Page<T> search(QueryBuilder var1, Pageable var2);

    Page<T> search(QueryBuilder var1, QueryBuilder var2, Pageable var3);

    Page<T> search(QueryBuilder var1, Pageable var2, String... var3);

    Page<T> search(QueryBuilder var1, QueryBuilder var2, Pageable var3, String... var4);

    List<T> search(QueryBuilder var1, int var2, List<SortBuilder> var3);

    List<T> search(QueryBuilder var1, int var2, int var3);

    List<T> search(QueryBuilder var1, QueryBuilder var2, int var3, int var4);

    List<T> search(QueryBuilder var1, int var2, int var3, List<SortBuilder> var4);

    FacetsResult searchAggs(QueryBuilder var1, String var2, AggregationBuilder var3, int var4, int var5, List<SortBuilder> var6);

    Map<String, Object> search(QueryBuilder var1, QueryBuilder var2, AggregationBuilder var3, Pageable var4, String... var5);

    List<List<T>> multiSearch(SearchRequestBuilder[] var1);

    long count(QueryBuilder var1);

    long count();

    T get(String var1);

    Map<String, T> multiGetToMap(String[] var1);

    List<T> multiGetToList(String[] var1);

    void deleteById(String var1);

    boolean deleteByIds(List<String> var1);

    boolean addByBean(T var1);

    boolean addByBeans(List<T> var1);

    List<T> getIndexData();

    void updateByBean(T var1);

    void upsertByBean(T var1);
}
