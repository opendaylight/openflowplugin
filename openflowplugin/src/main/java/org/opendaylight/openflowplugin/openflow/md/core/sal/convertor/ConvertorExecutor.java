/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.Collection;
import java.util.Optional;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData;
import org.opendaylight.yangtools.yang.binding.DataContainer;

public interface ConvertorExecutor {
    /**
     * Lookup and use convertor by specified type, then converts source and returns converted result
     *
     * @param <FROM> the source type
     * @param <TO>   the result type
     * @param <DATA> the data type
     * @param source the source
     * @param data   convertor data
     * @return the result (can be empty, if no convertor was found)
     */
    <FROM extends DataContainer, TO, DATA extends ConvertorData> Optional<TO> convert(final FROM source, final DATA data);

    /**
     * Lookup and use convertor by specified type, then converts source collection and returns converted result
     *
     * @param <FROM> the source type
     * @param <TO>   the result type
     * @param <DATA> the data type
     * @param source the source collection
     * @param data   convertor data
     * @return the result (can be empty, if no convertor was found)
     */
    <FROM extends DataContainer, TO, DATA extends ConvertorData> Optional<TO> convert(final Collection<FROM> source, final DATA data);
}