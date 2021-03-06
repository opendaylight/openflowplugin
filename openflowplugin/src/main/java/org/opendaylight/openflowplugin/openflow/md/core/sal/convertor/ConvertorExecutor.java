/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData;

public interface ConvertorExecutor {
    /**
     * Lookup and use convertor by specified type, then converts source and returns converted result.
     *
     * @param <F> the source type
     * @param <T>   the result type
     * @param <D> the data type
     * @param source the source
     * @param data   convertor data
     * @return the result (can be empty, if no convertor was found)
     */
    <F, T, D extends ConvertorData> Optional<T> convert(F source, D data);

    /**
     * Lookup and use convertor by specified type, then converts source collection and returns converted result.
     *
     * @param <F> the source type
     * @param <T>   the result type
     * @param <D> the data type
     * @param source the source collection
     * @param data   convertor data
     * @return the result (can be empty, if no convertor was found)
     */
    <F, T, D extends ConvertorData> Optional<T> convert(Collection<F> source, D data);

    /**
     * Lookup and use convertor by specified type, then converts source collection and returns converted result.
     *
     * @param <K> the source key type
     * @param <F> the source value type
     * @param <T>   the result type
     * @param <D> the data type
     * @param source the source collection
     * @param data   convertor data
     * @return the result (can be empty, if no convertor was found)
     */
    <K, F, T, D extends ConvertorData> Optional<T> convert(Map<K, F> source, D data);
}
