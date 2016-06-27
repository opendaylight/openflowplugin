/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

/**
 * Converts OpenflowJava to MDSal model and vice versa
 *
 * @param <FROM> type of source
 * @param <TO>   type of result
 */
public interface Convertor<FROM, TO> {

    /**
     * Gets type of convertor, used in
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager}.
     *
     * @return the type of convertor
     */
    Class<?> getType();

    /**
     * Converts source to result
     *
     * @param source source
     * @return converted source
     */
    TO convert(FROM source);
}
