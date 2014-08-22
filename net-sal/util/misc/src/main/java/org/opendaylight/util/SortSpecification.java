/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Sort specification.
 *
 * @param <T> type of the attribute to sort by.
 * 
 * @author Scott Simes
 * @author Fabiel Zuniga
 */
public class SortSpecification<T> {

    private List<SortComponent<T>> sortComponents;

    /**
     * Creates a sort specification.
     */
    public SortSpecification() {
        this.sortComponents = new ArrayList<SortSpecification.SortComponent<T>>();
    }

    /**
     * Appends a sort component to the collection of sort components for this 
     * specification
     * 
     * @param attribute attribute to sort by.
     * @param sortOrder sort direction.
     */
    public void addSortComponent(T attribute, SortOrder sortOrder) {
        this.sortComponents.add(new SortComponent<T>(attribute, sortOrder));
    }

    /**
     * Gets the specification's sort entries.
     * 
     * @return the specification's sort entries.
     */
    public List<SortComponent<T>> getSortComponents() {
        return Collections.unmodifiableList(this.sortComponents);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((sortComponents == null) ? 0 : sortComponents.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof SortSpecification))
            return false;
        SortSpecification<?> other = (SortSpecification<?>) obj;
        if (sortComponents == null) {
            if (other.sortComponents != null)
                return false;
        } else if (!sortComponents.equals(other.sortComponents))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append(this.getClass().getSimpleName());
        str.append('[');
        str.append("components=");
        str.append(this.sortComponents);
        str.append(']');

        return str.toString();
    }

    /**
     * Sort specification component.
     *
     * @param <T> type of the attribute to sort by.
     */
    public static class SortComponent<T> {
        private T sortBy;
        private SortOrder sortOrder;

        private SortComponent(T sortBy, SortOrder sortOrder) {
            this.sortBy = sortBy;
            this.sortOrder = sortOrder;
        }

        /**
         * Gets the attribute to sort by.
         * 
         * @return the attribute to sort by.
         */
        public T getSortBy() {
            return this.sortBy;
        }

        /**
         * Gets the sort direction.
         * 
         * @return the sort direction.
         */
        public SortOrder getSortOrder() {
            return this.sortOrder;
        }
        
       @Override
       public int hashCode() {
           final int prime = 31;
           int result = 1;
           result = prime * result
                   + ((sortBy == null) ? 0 : sortBy.hashCode());
           result = prime * result
                   + ((sortOrder == null) ? 0 : sortOrder.hashCode());
           return result;
       }

       @Override
       public boolean equals(Object obj) {
           if (this == obj)
               return true;
           if (obj == null)
               return false;
           if (!(obj instanceof SortComponent))
               return false;
           SortComponent<?> other = (SortComponent<?>) obj;
           if (sortBy == null) {
               if (other.sortBy != null)
                   return false;
           } else if (!sortBy.equals(other.sortBy))
               return false;
           if (sortOrder != other.sortOrder)
               return false;
           return true;
       }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();

            str.append('[');
            str.append("sortBy=");
            str.append(this.sortBy);
            str.append(", sortOrder=");
            str.append(this.sortOrder);
            str.append(']');

            return str.toString();
        }
    }
}

