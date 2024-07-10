/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * Convert action from MD-SAL model into OFJava-API model.
 *
 * @param <F> input message model - MD-SAL model
 * @param <T> output message model - OFJava-API
 */
public interface ConvertorActionToOFJava<F extends Action, T extends DataContainer> {

    /**
     * Convert OF action MD-SAL model.
     *
     * @param actionCase where is vendor's augmentation
     * @return message converted to OFJava-API
     */
    T convert(F actionCase);
}
