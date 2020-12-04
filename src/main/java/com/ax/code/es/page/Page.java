package com.ax.code.es.page;

import java.util.Iterator;
import java.util.List;

/**
 * @author lj
 */
public interface Page<T> extends Iterable<T> {

    int getNumber();

    int getSize();

    int getTotalPages();

    int getNumberOfElements();

    long getTotalElements();

    boolean hasPreviousPage();

    boolean isFirstPage();

    boolean hasNextPage();

    boolean isLastPage();

    Iterator<T> iterator();

    List<T> getContent();

    boolean hasContent();

    Sort getSort();

}
