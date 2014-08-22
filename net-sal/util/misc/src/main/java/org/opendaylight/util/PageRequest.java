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
 * objects to be returned per page
 * 
 * @author Scott Simes
 * @author Fabile Zuniga
 */
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -6821302246254693485L;

    private int pageIndex;
    private int pageSize;

    /**
     * Creates a request for the first page.
     * 
     * @param pageSize page size (maximum number of data items per page).
     */
    public PageRequest(int pageSize) {
        this(0, pageSize);
    }

    /**
     * Creates a page request.
     * 
     * @param pageIndex page index.
     * @param pageSize page size (maximum number of data items per page).
     * @throws IllegalArgumentException if {@code pageIndex} is less than zero.
     */
    public PageRequest(int pageIndex, int pageSize) throws IllegalArgumentException {
        if (pageIndex < 0) {
            throw new IllegalArgumentException("page index must be greater or equal to zero");
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pageIndex;
        result = prime * result + pageSize;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof PageRequest))
            return false;
        PageRequest other = (PageRequest) obj;
        if (pageIndex != other.pageIndex)
            return false;
        if (pageSize != other.pageSize)
            return false;
        return true;
    }
}
