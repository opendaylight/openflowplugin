/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.io.Serializable;

/**
 * Page request containing the specific page index desired, and the number of
 * objects to be returned per page.  This page request using a "one based" index
 * into the array of available pages, with the first available page referenced
 * as slot 1 into the array. 
 * 
 * @author Scott Simes
 * @author Fabiel Zuniga
 */
public class OneBasedPageRequest implements Serializable {
    private static final long serialVersionUID = -6821302246254693485L;

    private int pageIndex;
    private int pageSize;

    /**
     * Creates a request for the first page.
     * 
     * @param pageSize page size (maximum number of data items per page).
     */
    public OneBasedPageRequest(int pageSize) {
        this(1, pageSize);
    }
    
    /**
     * Creates a request for One based data based on the supplied page request
     * @param zeroBasedRequest the zero based page request to be represented as 
     * a one based page request
     */
    public OneBasedPageRequest(PageRequest zeroBasedRequest) {
        this((zeroBasedRequest.getPageIndex() + 1), zeroBasedRequest.getPageSize());
    }

    /**
     * Creates a page request.
     * 
     * @param pageIndex page index.
     * @param pageSize page size (maximum number of data items per page).
     * @throws IllegalArgumentException if {@code pageIndex} is less than one.
     */
    public OneBasedPageRequest(int pageIndex, int pageSize) throws IllegalArgumentException {
        if (pageIndex < 1) {
            throw new IllegalArgumentException("page index must be greater or equal to one");
        }
        
        if(pageSize <= 0) {
            throw new IllegalArgumentException("page size must be greater than 0");
        }

        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    /**
     * Gets the page index.
     * 
     * @return the page index.
     */
    public int getPageIndex() {
        return this.pageIndex;
    }

    /**
     * Gets the page size (maximum number of data items per page).
     * 
     * @return the page size.
     */
    public int getPageSize() {
        return this.pageSize;
    }
    
    /**
     * Converts this One based page request to a zero based page request
     * @return a zero based page request
     */
    public PageRequest toPageRequest() {
        return new PageRequest((pageIndex -1), pageSize);
    }
}
