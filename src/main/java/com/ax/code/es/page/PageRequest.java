package com.ax.code.es.page;

import java.io.Serializable;

/**
 * @author lj
 */
public class PageRequest implements Pageable, Serializable {
    private static final long serialVersionUID = 8280485938848398236L;
    private int page;
    private int size;
    private Sort sort;

    public PageRequest() {
        this.page = 0;
        this.size = 10;
    }

    public PageRequest(int page, int size) {
        this(page, size, (Sort) null);
    }

    public PageRequest(int page, int size, Sort.Direction direction, String... properties) {
        this(page, size, new Sort(direction, properties));
    }

    public PageRequest(int page, int size, Sort sort) {
        this.page = 0;
        this.size = 10;
        if (0 > page) {
            throw new IllegalArgumentException("Page index must not be less than zero!");
        } else if (0 >= size) {
            throw new IllegalArgumentException("Page size must not be less than or equal to zero!");
        } else {
            this.setPage(page);
            this.setSize(size);
            this.sort = sort;
        }
    }

    @Override
    public int getPageSize() {
        return this.size;
    }

    @Override
    public int getPageNumber() {
        return this.page;
    }

    @Override
    public int getOffset() {
        return this.page > 0 ? (this.page - 1) * this.size : 0;
    }

    @Override
    public Sort getSort() {
        return this.sort;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PageRequest)) {
            return false;
        } else {
            PageRequest that = (PageRequest) obj;
            boolean pEqual = this.page == that.page;
            boolean sEqual = this.size == that.size;
            boolean sortEqual = this.sort == null ? that.sort == null : this.sort.equals(that.sort);
            return pEqual && sEqual && sortEqual;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.page;
        result = 31 * result + this.size;
        result = 31 * result + (null == this.sort ? 0 : this.sort.hashCode());
        return result;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int p) {
        this.page = p < 1 ? 0 : p;
        this.page = p > 99 ? 99 : p;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }
}
