/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import java.util.Set;

/**
 * Provides {@link Facet} implementations.
 * 
 * @author Simon Hunt
 */
public interface FacetProvider {

    /**
     * Returns the set of facet classes for the facets that this provider
     * provides.
     * 
     * @return the set of facet classes
     */
    public Set<Class<? extends Facet>> getFacetClasses();

    /**
     * Returns the set of facet class names for the facets that this provider
     * provides. Implementations should iterate across their implementation of
     * {@link #getFacetClasses} and populate the returned set with the result
     * of calling {@code Class.getName()} on each class.
     * 
     * @return the set of facet class names
     */
    public Set<String> getFacetClassNames();

    /**
     * Returns true if the specified facet class is supported by this
     * provider. The following implementation (or its equivalent) should hold
     * true:
     * 
     * <pre>
     * public boolean isSupported(Class&lt;? extends Facet&gt; facetClass) {
     *     return {@link #getFacetClasses}().contains(facetClass);
     * }
     * </pre>
     * 
     * @param facetClass a facet class
     * @return true if this provider supports that class of facet
     */
    public boolean isSupported(Class<? extends Facet> facetClass);

    /**
     * Returns an implementation of the specified facet class, or null if that
     * facet is not supported by this provider.
     * 
     * @param <T> Facet type
     * @param facetClass the class of the required facet
     * @return a facet implementation
     */
    public <T extends Facet> T getFacet(Class<T> facetClass);

}
