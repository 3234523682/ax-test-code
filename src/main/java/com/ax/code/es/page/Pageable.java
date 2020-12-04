package com.ax.code.es.page;

/**
 * @author lj
 */
public interface Pageable {
    int getPageNumber();

    int getPageSize();

    int getOffset();

    Sort getSort();
}