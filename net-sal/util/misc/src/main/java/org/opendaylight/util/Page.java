/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data page contains the information representing a single page of data. The
 * data values represented on the page, as well as the index into the list of
 * pages represented by this page, the total number of records available, and
 * the total number of pages possible.
 * 
 * @param <D> type of the data.
 * @author Scott Simes
 * @author Fabiel Zuniga
 */
public class Page<D> implements Serializable {

    private static final long serialVersionUID = -155675023409629660L;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final Page EMPTY_PAGE = new Page(Collections.emptyList(),
                                                    new PageRequest(0, 1), 0, 0);

    private List<D> data;
    private PageRequest pageRequest;
    private long pageCount;
    private long recordCount;

    /**
     * Creates a data page.
     * 
     * @param data page's data.
     * @param pageRequest request that generated this page.
     * @param totalRecordCount total number of records.
     * @param totalPageCount total number of pages.
     */
    public Page(List<D> data, PageRequest pageRequest, long totalRecordCount,
                long totalPageCount) {
        this.data = Collections.unmodifiableList(data);
        this.pageRequest = pageRequest;
        this.pageCount = totalPageCount;
        this.recordCount = totalRecordCount;
    }

    /**
     * Returns the empty page (immutable).
     * 
     * @return an empty page.
     */
    @SuppressWarnings({ "cast", "unchecked" })
    public static final <T> Page<T> emptyPage() {
        return (Page<T>) EMPTY_PAGE;
    }
    
    /**
     * Determines if a page is empty (contains no data)
     * 
     * @return true if the page contains no data
     */
    public boolean isEmpty() {
        return this.data.size() <= 0;
    }

    /**
     * Gets the page data.
     * 
     * @return the page data.
     */
    public List<D> getData() {
        return this.data;
    }

    /**
     * Gets the request that generated this page.
     * 
     * @return the request that generated this page.
     */
    public PageRequest getPageRequest() {
        return this.pageRequest;
    }

    /**
     * Gets the total number of pages.
     * 
     * @return the total number of pages.
     */
    public long getPageCount() {
        return this.pageCount;
    }

    /**
     * Gets the total number of records.
     * 
     * @return the total number of records.
     */
    public long getRecordCount() {
        return this.recordCount;
    }

    /**
     * Converts a page to a different data type.
     * 
     * @param converter converter.
     * @return a page with the new data type.
     */
    public <T> Page<T> convert(Converter<D, T> converter) {
        List<T> targetItems = new ArrayList<T>(this.data.size());
        for (D item : this.data) {
            targetItems.add(converter.convert(item));
        }

        return new Page<T>(targetItems, this.pageRequest, this.recordCount,
                           this.pageCount);
    }

    /**
     * Checks whether there is a page after this one.
     * 
     * @return {@code true} if there is a page after this one, {@code false}
     *         otherwise.
     */
    public boolean hasNext() {
        return this.pageRequest.getPageIndex() < (this.pageCount - 1);
    }

    /**
     * Checks whether there is a page previous to this one.
     * 
     * @return {@code true} if there is a page previous to this one,
     *         {@code false} otherwise.
     */
    public boolean hasPrevious() {
        return this.pageRequest.getPageIndex() > 0;
    }

    /**
     * Creates a request for the next page.
     * 
     * @return a request for the next page if there is a page, {@code null}
     *         otherwise.
     */
    public PageRequest getNextPageRequest() {
        PageRequest request = null;

        if (hasNext()) {
            request = new PageRequest(this.pageRequest.getPageIndex() + 1,
                                      this.pageRequest.getPageSize());
        }

        return request;
    }

    /**
     * Creates a request for the previous page.
     * 
     * @return a request for the previous page if there is a page,
     *         {@code null} otherwise.
     */
    public PageRequest getPreviousPageRequest() {
        PageRequest request = null;

        if (hasPrevious()) {
            request = new PageRequest(this.pageRequest.getPageIndex() - 1,
                                      this.pageRequest.getPageSize());
        }

        return request;
    }
}
