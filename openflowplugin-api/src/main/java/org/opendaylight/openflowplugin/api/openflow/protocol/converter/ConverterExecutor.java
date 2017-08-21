/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.protocol.converter;

import java.util.Collection;
import java.util.Optional;

public interface ConverterExecutor {
    /**
     * Lookup and use converter by specified type, then converts source and returns converted result.
     *
     * @param <F> the source type
     * @param <T>   the result type
     * @param <D> the data type
     * @param source the source
     * @param data   converter data
     * @return the result (can be empty, if no converter was found)
     */
    <F, T, D extends ConverterData> Optional<T> convert(F source, D data);

    /**
     * Lookup and use converter by specified type, then converts source collection and returns converted result,
     *
     * @param <F> the source type
     * @param <T>   the result type
     * @param <D> the data type
     * @param source the source collection
     * @param data   converter data
     * @return the result (can be empty, if no converter was found)
     */
    <F, T, D extends ConverterData> Optional<T> convert(Collection<F> source, D data);
}