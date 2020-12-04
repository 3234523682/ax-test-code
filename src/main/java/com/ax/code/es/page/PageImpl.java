package com.ax.code.es.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author lj
 */
public class PageImpl<T> implements Page<T>, Serializable {
    private static final long serialVersionUID = 867755909294344406L;
    private final List<T> content;
    private final Pageable pageable;
    private final long total;

    public PageImpl(List<T> content, Pageable pageable, long total) {
        this.content = new ArrayList();
        if (content != null) {
            this.content.addAll(content);
        }

        this.total = total;
        this.pageable = pageable;
    }

    public PageImpl(List<T> content) {
        this(content, (Pageable) null, null == content ? 0L : (long) content.size());
    }

    @Override
    public int getNumber() {
        return this.pageable == null ? 0 : this.pageable.getPageNumber();
    }

    @Override
    public int getSize() {
        return this.pageable == null ? 0 : this.pageable.getPageSize();
    }

    @Override
    public int getTotalPages() {
        return this.getSize() == 0 ? 0 : (int) Math.ceil((double) this.total / (double) this.getSize());
    }

    @Override
    public int getNumberOfElements() {
        return this.content.size();
    }

    @Override
    public long getTotalElements() {
        return this.total;
    }

    @Override
    public boolean hasPreviousPage() {
        return this.getNumber() > 0;
    }

    @Override
    public boolean isFirstPage() {
        return !this.hasPreviousPage();
    }

    @Override
    public boolean hasNextPage() {
        return (long) ((this.getNumber() + 1) * this.getSize()) < this.total;
    }

    @Override
    public boolean isLastPage() {
        return !this.hasNextPage();
    }

    @Override
    public Iterator<T> iterator() {
        return this.content.iterator();
    }

    @Override
    public List<T> getContent() {
        return Collections.unmodifiableList(this.content);
    }

    @Override
    public boolean hasContent() {
        return !this.content.isEmpty();
    }

    @Override
    public Sort getSort() {
        return this.pageable == null ? null : this.pageable.getSort();
    }

    @Override
    public String toString() {
        String contentType = "UNKNOWN";
        if (this.content.size() > 0) {
            contentType = this.content.get(0).getClass().getName();
        }

        return String.format("Page %s of %d containing %s instances", this.getNumber(), this.getTotalPages(), contentType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PageImpl)) {
            return false;
        } else {
            PageImpl<?> that = (PageImpl) obj;
            boolean totalEqual = this.total == that.total;
            boolean contentEqual = this.content.equals(that.content);
            boolean pageableEqual = this.pageable == null ? that.pageable == null : this.pageable.equals(that.pageable);
            return totalEqual && contentEqual && pageableEqual;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (int) (this.total ^ this.total >>> 32);
        result = 31 * result + (this.pageable == null ? 0 : this.pageable.hashCode());
        result = 31 * result + this.content.hashCode();
        return result;
    }
}
