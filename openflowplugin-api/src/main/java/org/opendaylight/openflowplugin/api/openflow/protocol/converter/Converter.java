/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.protocol.converter;

import java.util.Collection;

/**
 * Converts OpenflowJava to MDSal model and vice versa
 *
 * @param <FROM> type of source
 * @param <TO>   type of result
 * @param <DATA> the type parameter
 */
public abstract class Converter<FROM, TO, DATA extends ConverterData> {
    private ConverterExecutor converterExecutor;

    /**
     * Gets converter manager.
     *
     * @return the converter manager
     */
    protected ConverterExecutor getConverterExecutor() {
        return converterExecutor;
    }

    /**
     * Sets converter manager.
     *
     * @param converterExecutor the converter manager
     */
    public void setConverterExecutor(ConverterExecutor converterExecutor) {
        this.converterExecutor = converterExecutor;
    }

    /**
     * Gets type of converter.
     *
     * @return the type of converter
     */
    public abstract Collection<Class<?>> getTypes();

    /**
     * Converts source to result
     *
     * @param source source
     * @param data   converter data
     * @return converted source
     */
    public abstract TO convert(FROM source, DATA data);
}