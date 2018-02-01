/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import java.util.Collection;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;

/**
 * Converts OpenflowJava to MDSal model and vice versa.
 *
 * @param <F> type of source
 * @param <T>   type of result
 * @param <D> the type parameter
 */
public abstract class Convertor<F, T, D extends ConvertorData> {
    private ConvertorExecutor convertorExecutor;

    /**
     * Gets convertor manager.
     *
     * @return the convertor manager
     */
    protected ConvertorExecutor getConvertorExecutor() {
        return convertorExecutor;
    }

    /**
     * Sets convertor manager.
     *
     * @param convertorExecutor the convertor manager
     */
    public void setConvertorExecutor(ConvertorExecutor convertorExecutor) {
        this.convertorExecutor = convertorExecutor;
    }

    /**
     * Gets type of convertor, used in
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager}.
     *
     * @return the type of convertor
     */
    public abstract Collection<Class<?>> getTypes();

    /**
     * Converts source to result.
     *
     * @param source source
     * @param data   convertor data
     * @return converted source
     */
    public abstract T convert(F source, D data);
}
