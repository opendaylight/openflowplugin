/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.converter;

import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.common.Converter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.common.ConverterData;

public interface ConverterRegistration {
    /**
     * Register converter.
     *
     * @param converter the converter
     */
    ConverterManager registerConverter(final short version, final Converter<?, ?, ? extends ConverterData> converter);
}