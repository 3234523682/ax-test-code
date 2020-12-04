package com.ax.code.es.esservice;

import com.ax.code.es.annotation.EsIndex;
import com.ax.code.es.annotation.IndexField;
import com.ax.code.es.exception.NotEsIndexException;
import com.ax.code.es.page.Page;
import com.ax.code.es.page.PageImpl;
import com.ax.code.es.page.Pageable;
import com.ax.code.es.page.Sort;
import com.ax.code.es.utils.FacetsGet;
import com.ax.code.es.utils.FacetsResult;
import com.ax.code.es.utils.HighlightUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.format.StrictISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lj
 */
public abstract class AbstractEsService<T> implements EsService<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEsService.class);
    private static final String DEFAULT_ID_FIELD_NAME = "_id";
    private Class<T> clazz;
    private Field[] fields;
    private String indexName;
    private String type;
    private int shards;
    private int replicas;
    private String refreshInterval;
    protected Client client;

    public AbstractEsService() {
        this.init();
    }

    @Override
    public Map<String, Object> search(QueryBuilder query, QueryBuilder filter, AggregationBuilder aggs, Pageable page, String... highlightFields) {
        SearchRequestBuilder searchRequest = this.client.prepareSearch(new String[]{this.getIndexName()}).setTypes(new String[]{this.getType()});
        if (filter != null) {
            searchRequest.setPostFilter(filter);
        }

        if (aggs != null) {
            searchRequest.addAggregation(aggs);
        }

        List<SortBuilder> sorts = this.ParsePageSort(page.getSort());
        if (sorts != null) {
            Iterator var8 = sorts.iterator();

            while (var8.hasNext()) {
                SortBuilder sort = (SortBuilder) var8.next();
                searchRequest.addSort(sort);
            }
        }

        if (highlightFields != null) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            String[] var19 = highlightFields;
            int var10 = highlightFields.length;

            for (int var11 = 0; var11 < var10; ++var11) {
                String field = var19[var11];
                highlightBuilder.field(field, 70).requireFieldMatch(true);
            }

            searchRequest.highlighter(highlightBuilder);
        }

        int start = page.getOffset();
        int size = page.getPageSize();
        searchRequest.setFrom(start).setSize(size).setQuery(query);
        SearchResponse searchResponse = (SearchResponse) searchRequest.execute().actionGet();
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        long total = searchHits.getTotalHits();
        Page<T> result = this.getPagingHits(hits, page, total);
        Map map = new HashMap();
        map.put("page", result);
        map.put("aggs", searchResponse.getAggregations());
        return map;
    }

    private void init() {
        try {
            Class<? extends AbstractEsService> aClass = this.getClass();
            this.clazz = getSuperClassGenricType(aClass, 0);
            if (this.clazz.equals(Object.class)) {
                return;
            }

            this.fields = this.clazz.getDeclaredFields();
            Field.setAccessible(this.fields, true);
            EsIndex esIndex = (EsIndex) this.clazz.getAnnotation(EsIndex.class);
            if (esIndex == null) {
                throw new NotEsIndexException();
            }

            this.setIndexName(esIndex.indexName());
            this.setType(esIndex.type());
            this.shards = esIndex.shards();
            this.replicas = esIndex.replicas();
            this.refreshInterval = esIndex.refreshInterval();
        } catch (Exception var3) {
            LOG.error("init error:", var3);
        }

    }

    public SearchHits internalSearch(QueryBuilder query, int start, int size, List<SortBuilder> sorts, String... highlightFields) {
        return this.internalSearch(query, (QueryBuilder) null, start, size, sorts, highlightFields);
    }

    public SearchHits internalSearch(QueryBuilder query, QueryBuilder filter, int start, int size, List<SortBuilder> sorts, String... highlightFields) {
        if (this.client == null) {
            LOG.error("Elasticsearch Client may not be null!");
        }

        SearchRequestBuilder searchRequest = this.client.prepareSearch(new String[]{this.getIndexName()}).setTypes(new String[]{this.getType()});
        if (sorts != null) {
            Iterator var8 = sorts.iterator();

            while (var8.hasNext()) {
                SortBuilder sort = (SortBuilder) var8.next();
                searchRequest.addSort(sort);
            }
        }

        if (highlightFields != null) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            String[] var15 = highlightFields;
            int var10 = highlightFields.length;

            for (int var11 = 0; var11 < var10; ++var11) {
                String field = var15[var11];
                highlightBuilder.field(field, 70).requireFieldMatch(true);
            }

            searchRequest.highlighter(highlightBuilder);
        }

        if (filter != null) {
            searchRequest.setPostFilter(filter);
        }

        searchRequest.setFrom(start).setSize(size).setQuery(query);
        SearchResponse searchResponse = (SearchResponse) searchRequest.execute().actionGet();
        SearchHits searchHits = searchResponse.getHits();
        return searchHits;
    }

    @Override
    public FacetsResult searchAggs(QueryBuilder query, String key, AggregationBuilder aggs, int start, int size, List<SortBuilder> sorts) {
        if (this.client == null) {
            LOG.error("Elasticsearch Client may not be null!");
        }

        SearchRequestBuilder searchRequest = this.client.prepareSearch(new String[]{this.getIndexName()}).setTypes(new String[]{this.getType()});
        if (sorts != null) {
            Iterator var8 = sorts.iterator();

            while (var8.hasNext()) {
                SortBuilder sort = (SortBuilder) var8.next();
                searchRequest.addSort(sort);
            }
        }

        searchRequest.setFrom(start).setSize(size).setQuery(query).addAggregation(aggs);
        SearchResponse searchResponse = (SearchResponse) searchRequest.execute().actionGet();
        FacetsResult result = (new FacetsGet()).getFacets(searchResponse, key, aggs);
        return result;
    }

    @Override
    public Page<T> search(QueryBuilder query, Pageable page) {
        return this.search(query, (Pageable) page, (String[]) null);
    }

    @Override
    public Page<T> search(QueryBuilder query, QueryBuilder filter, Pageable page) {
        return this.search(query, filter, page, (String[]) null);
    }

    @Override
    public Page<T> search(QueryBuilder query, Pageable page, String... highlightFields) {
        return this.search(query, (QueryBuilder) null, page, highlightFields);
    }

    @Override
    public Page<T> search(QueryBuilder query, QueryBuilder filter, Pageable page, String... highlightFields) {
        int start = page.getOffset();
        int size = page.getPageSize();
        List<SortBuilder> sorts = this.ParsePageSort(page.getSort());
        SearchHits searchHits = this.internalSearch(query, filter, start, size, sorts, highlightFields);
        SearchHit[] hits = searchHits.getHits();
        long total = searchHits.getTotalHits();
        Page<T> result = this.getPagingHits(hits, page, total);
        return result;
    }

    @Override
    public List<T> search(QueryBuilder query, int size, List<SortBuilder> sorts) {
        SearchHits searchHits = this.internalSearch(query, 0, size, sorts);
        return this.getEntityList(searchHits.getHits());
    }

    @Override
    public List<T> search(QueryBuilder query, int start, int size) {
        return this.search(query, (QueryBuilder) null, start, size);
    }

    @Override
    public List<T> search(QueryBuilder query, QueryBuilder filter, int start, int size) {
        SearchHits searchHits = this.internalSearch(query, filter, start, size, (List) null);
        return this.getEntityList(searchHits.getHits());
    }

    @Override
    public List<T> search(QueryBuilder query, int start, int size, List<SortBuilder> sorts) {
        SearchHits searchHits = this.internalSearch(query, start, size, sorts);
        return this.getEntityList(searchHits.getHits());
    }

    public List<T> search(QueryBuilder query, int start, int size, List<SortBuilder> sorts, String... highlight) {
        SearchHits searchHits = this.internalSearch(query, start, size, sorts, highlight);
        return this.getEntityList(searchHits.getHits());
    }

    @Override
    public long count(QueryBuilder query) {
        return ((SearchResponse) this.client.prepareSearch(new String[]{this.getIndexName()}).setTypes(new String[]{this.getType()}).setSize(0).setQuery(query).get()).getHits().getTotalHits();
    }

    @Override
    public long count() {
        return this.count(QueryBuilders.matchAllQuery());
    }

    @Override
    public T get(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        } else {
            GetResponse response = (GetResponse) this.client.prepareGet(this.getIndexName(), this.getType(), id).execute().actionGet();
            return this.constructEntity(response.getId(), response.getSource());
        }
    }

    @Override
    public List<List<T>> multiSearch(SearchRequestBuilder[] requests) {
        List<List<T>> result = new ArrayList();
        MultiSearchRequestBuilder builder = this.client.prepareMultiSearch();
        SearchRequestBuilder[] var4 = requests;
        int var5 = requests.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            SearchRequestBuilder request = var4[var6];
            builder.add(request);
        }

        MultiSearchResponse response = (MultiSearchResponse) builder.execute().actionGet();
        MultiSearchResponse.Item[] items = response.getResponses();
        MultiSearchResponse.Item[] var14 = items;
        int var15 = items.length;

        for (int var8 = 0; var8 < var15; ++var8) {
            MultiSearchResponse.Item item = var14[var8];
            if (!item.isFailure()) {
                SearchResponse sResponse = item.getResponse();
                List<T> entities = this.getEntityList(sResponse.getHits().hits());
                result.add(entities);
            }
        }

        return result;
    }

    @Override
    public Map<String, T> multiGetToMap(String[] ids) {
        Map<String, T> entities = new HashMap();
        MultiGetResponse response = (MultiGetResponse) this.client.prepareMultiGet().add(this.getIndexName(), this.getType(), ids).execute().actionGet();
        MultiGetItemResponse[] responses = response.getResponses();
        MultiGetItemResponse[] var5 = responses;
        int var6 = responses.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            MultiGetItemResponse result = var5[var7];
            if (!result.isFailed()) {
                GetResponse getResponse = result.getResponse();
                String id = getResponse.getId();
                T entity = this.constructEntity(id, getResponse.getSource());
                entities.put(id, entity);
            }
        }

        return entities;
    }

    @Override
    public List<T> multiGetToList(String[] ids) {
        List<T> entities = new ArrayList();
        MultiGetResponse response = (MultiGetResponse) this.client.prepareMultiGet().add(this.getIndexName(), this.getType(), ids).execute().actionGet();
        MultiGetItemResponse[] responses = response.getResponses();
        MultiGetItemResponse[] var5 = responses;
        int var6 = responses.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            MultiGetItemResponse result = var5[var7];
            if (!result.isFailed()) {
                GetResponse getResponse = result.getResponse();
                String id = getResponse.getId();
                Map<String, Object> source = getResponse.getSource();
                if (source != null) {
                    T entity = this.constructEntity(id, source);
                    entities.add(entity);
                }
            }
        }

        return entities;
    }

    public List<T> moreLikeThisQuery(MoreLikeThis mlt) {
        return null;
    }

    @Override
    public void deleteById(String id) {
        DeleteResponse response = (DeleteResponse) this.client.prepareDelete(this.getIndexName(), this.getType(), id).execute().actionGet();
        LOG.info("Delete " + this.getIndexName() + " " + id);
    }

    @Override
    public boolean deleteByIds(List<String> ids) {
        BulkRequestBuilder bulk = this.client.prepareBulk();
        Iterator var3 = ids.iterator();

        while (var3.hasNext()) {
            String id = (String) var3.next();
            bulk.add(new DeleteRequest(this.getIndexName(), this.getType(), id));
            LOG.info("Delete " + this.getIndexName() + " " + id);
        }

        if (bulk.numberOfActions() > 0) {
            BulkResponse response = (BulkResponse) bulk.execute().actionGet();
            if (response.hasFailures()) {
                LOG.error(response.buildFailureMessage());
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addByBean(T bean) {
        return this.index(this.entityToMap(bean));
    }

    @Override
    public boolean addByBeans(List<T> beans) {
        BulkRequestBuilder bulk = this.client.prepareBulk();
        Iterator<T> var3 = beans.iterator();

        while (var3.hasNext()) {
            T t = var3.next();
            Map<String, Object> source = this.entityToMap(t);
            String id = (String) source.get("id");
            bulk.add((new IndexRequest(this.getIndexName(), this.getType(), id)).source(source));
        }

        if (bulk.numberOfActions() > 0) {
            BulkResponse response = (BulkResponse) bulk.execute().actionGet();
            if (response.hasFailures()) {
                LOG.error(response.buildFailureMessage());
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateByBean(T entity) {
        Map<String, Object> doc = this.entityToMap(entity);
        String id = (String) doc.get("id");
        UpdateRequestBuilder updateRequestBuilder = this.client.prepareUpdate(this.getIndexName(), this.getType(), id);
        updateRequestBuilder.setDoc(doc).execute().actionGet();
    }

    @Override
    public void upsertByBean(T entity) {
        Map<String, Object> doc = this.entityToMap(entity);
        String id = (String) doc.get("id");
        UpdateRequestBuilder updateRequestBuilder = this.client.prepareUpdate(this.getIndexName(), this.getType(), id);
        updateRequestBuilder.setDoc(doc).setUpsert(doc).execute().actionGet();
    }

    public boolean index(Map<String, Object> doc) {
        try {
            String id = (String) doc.get("id");
            IndexRequestBuilder indexRequestBuilder = this.client.prepareIndex(this.getIndexName(), this.getType(), id);
            indexRequestBuilder.setSource(doc);
            IndexResponse response = (IndexResponse) indexRequestBuilder.execute().actionGet();
            String indexedId = response.getId();
            return StringUtils.isNotBlank(indexedId);
        } catch (Exception var6) {
            LOG.error("add index error", var6);
            return false;
        }
    }

    public Map<String, Object> entityToMap(T entity) {
        Field[] superfields = this.clazz.getSuperclass().getDeclaredFields();
        Field[] fields = this.clazz.getDeclaredFields();
        Map<String, Object> doc = new HashMap();
        this.setValue(superfields, doc, entity);
        this.setValue(fields, doc, entity);
        return doc;
    }

    private void setValue(Field[] fields, Map<String, Object> doc, T entity) {
        for (int i = 0; i < fields.length; ++i) {
            fields[i].setAccessible(true);

            try {
                IndexField field = (IndexField) fields[i].getAnnotation(IndexField.class);
                if (field != null) {
                    String fieldName = field.value();
                    if (StringUtils.isEmpty(fieldName)) {
                        fieldName = fields[i].getName();
                    }

                    Object value = fields[i].get(entity);
                    if (value != null && field.isObjectDataType()) {
                        String typeName = fields[i].getGenericType().getTypeName();
                        Class<?> aClass = Class.forName(typeName);
                        value = setValue(aClass.getDeclaredFields(), value);
                    }

                    if (value != null && field.isNested()) {
                        value = setListValue(value);
                    }

                    if (value != null) {
                        doc.put(fieldName, value);
                    }
                }
            } catch (Exception var10) {
                LOG.error("setValue errors:", var10);
            }
        }

    }

    private static Map<String, Object> setValue(Field[] fields, Object entity) {
        Map<String, Object> map = new HashMap();

        for (int i = 0; i < fields.length; ++i) {
            Field field = fields[i];
            if (!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);

                try {
                    String fieldName = field.getName();
                    Object value = field.get(entity);
                    if (value != null) {
                        map.put(fieldName, value);
                    }
                } catch (Exception var7) {
                    LOG.error("setValue errors:", var7);
                }
            }
        }

        return map;
    }

    private static List setListValue(Object obj) {
        List list = (List) obj;
        List mapList = new ArrayList();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); ++i) {
                Object o = list.get(i);
                Field[] fields = o.getClass().getDeclaredFields();
                Map<String, Object> map = new HashMap();

                for (int k = 0; k < fields.length; ++k) {
                    Field field = fields[k];
                    if (!Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);

                        try {
                            String fieldName = field.getName();
                            Object value = field.get(o);
                            if (value != null) {
                                map.put(fieldName, value);
                            }
                        } catch (Exception var11) {
                            LOG.error("setValue errors:", var11);
                        }
                    }
                }

                mapList.add(map);
            }
        }

        return mapList;
    }

    private List<SortBuilder> ParsePageSort(Sort sort) {
        List<SortBuilder> sorts = null;
        if (sort != null) {
            sorts = new ArrayList();
            Iterator orders = sort.iterator();

            while (orders.hasNext()) {
                Sort.Order order = (Sort.Order) orders.next();
                SortBuilder esSort = buildSort(order.getProperty(), order.getDirection().toString());
                sorts.add(esSort);
            }
        }

        return sorts;
    }

    private Page<T> getPagingHits(SearchHit[] hits, Pageable pageable, long total) {
        List<T> results = this.getEntityList(hits);
        Page<T> page = new PageImpl(results, pageable, total);
        return page;
    }

    private List<T> getEntityList(SearchHit[] hits) {
        List<T> entityList = new ArrayList();
        SearchHit[] var3 = hits;
        int var4 = hits.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            SearchHit hit = var3[var5];

            try {
                T entity = this.constructEntity(hit);
                entityList.add(entity);
            } catch (Exception var8) {
                var8.printStackTrace();
            }
        }

        return entityList;
    }

    private T constructEntity(SearchHit hit) {
        return this.constructEntity(hit.getId(), hit.getSource(), hit.getHighlightFields());
    }

    private T constructEntity(String id, Map<String, Object> source) {
        return (T) this.constructEntity(id, source, (Map) null);
    }

    private T constructEntity(String id, Map<String, Object> source, Map<String, HighlightField> highlightFields) {
        try {
            Object obj = this.clazz.newInstance();

            for (int k = 0; k < this.fields.length; ++k) {
                boolean isDefineField = this.fields[k].isAnnotationPresent(IndexField.class);
                if (isDefineField) {
                    IndexField actualField = (IndexField) this.fields[k].getAnnotation(IndexField.class);
                    String fieldName = actualField.value();
                    if (StringUtils.isEmpty(fieldName)) {
                        fieldName = this.fields[k].getName();
                    }

                    HighlightField highlightField = null;
                    if (highlightFields != null) {
                        highlightField = (HighlightField) highlightFields.get(fieldName);
                    }

                    Object value;
                    if (highlightField != null) {
                        value = HighlightUtils.textToString(highlightField.getFragments());
                    } else {
                        value = source.get(fieldName);
                    }

                    if (value != null && actualField.isObjectDataType()) {
                        this.fields[k].set(obj, this.constructEntity((Map) value, this.fields[k].getType().newInstance()));
                    } else if (value != null && actualField.isNested()) {
                        Type genericType = this.fields[k].getGenericType();
                        ParameterizedType pType = (ParameterizedType) genericType;
                        Type type = pType.getActualTypeArguments()[0];
                        this.fields[k].set(obj, this.constructListEntity((List) value, Class.forName(type.getTypeName())));
                    } else {
                        if (value == null) {
                            value = "";
                        }

                        this.setEntityField(obj, this.fields[k], value.toString());
                    }
                }
            }

            return (T) obj;
        } catch (Exception var14) {
            var14.printStackTrace();
            LOG.error("constructEntity error:", var14);
            return null;
        }
    }

    private Object constructEntity(Map<String, Object> source, Object obj) {
        try {
            Field[] fields = obj.getClass().getDeclaredFields();

            for (int k = 0; k < fields.length; ++k) {
                Field field = fields[k];
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object value = source.get(fieldName);
                    if (value == null) {
                        value = "";
                    }

                    this.setEntityField(obj, field, value.toString());
                }
            }

            return obj;
        } catch (Exception var8) {
            var8.printStackTrace();
            return null;
        }
    }

    private List constructListEntity(List<Map<String, Object>> obj, Class clazz) {
        List list = new ArrayList();
        if (obj != null && obj.size() > 0) {
            for (int i = 0; i < obj.size(); ++i) {
                try {
                    list.add(this.constructEntity((Map) obj.get(i), clazz.newInstance()));
                } catch (InstantiationException var6) {
                    var6.printStackTrace();
                } catch (IllegalAccessException var7) {
                    var7.printStackTrace();
                }
            }
        }

        return list;
    }

    private void setEntityField(Object obj, Field field, String value) {
        try {
            Class<?> type = field.getType();
            if (type.toString().endsWith("Date")) {
                if (StringUtils.isNotEmpty(value)) {
                    DateTime dateTime = StrictISODateTimeFormat.dateOptionalTimeParser().parseDateTime(value.toString());
                    field.set(obj, dateTime.toDate());
                } else {
                    field.set(obj, (Object) null);
                }
            } else {
                field.set(obj, ConvertUtils.convert(value, type));
            }
        } catch (Exception var6) {
            LOG.error("setEntityField error:", var6);
        }

    }

    public static SortBuilder buildSort(String field, String order) {
        SortBuilder sort = SortBuilders.fieldSort(field);
        if ("asc".equalsIgnoreCase(order)) {
            sort.order(SortOrder.ASC);
        } else {
            sort.order(SortOrder.DESC);
        }

        return sort;
    }

    public Class<T> getClazz() {
        return this.clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Client getClient() {
        return this.client;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public String getType() {
        return this.type;
    }

    protected void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    protected void setType(String type) {
        this.type = type;
    }

    protected abstract void setClient(Client var1);

    public static Class getSuperClassGenricType(Class clazz, int index) throws IndexOutOfBoundsException {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (index < params.length && index >= 0) {
                return !(params[index] instanceof Class) ? Object.class : (Class) params[index];
            } else {
                return Object.class;
            }
        }
    }
}
