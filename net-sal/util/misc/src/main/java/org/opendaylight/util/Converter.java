/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Converts data from the source type to the target type
 * 
 * @param <S> the type of the source data to convert
 * @param <T> the type of the target data to be converted to
 * @author Scott Simes
 * @author Fabiel Zuniga
 */
public interface Converter<S, T> {

    /**
     * Converts an instance of type S to an instance of type T
     * 
     * @param source the source object to be converted
     * @return an instance of the target type T converted from the source
     */
    public T convert(S source);
}
