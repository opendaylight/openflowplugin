/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

/**
 * @author tkubas
 * @param <I> input message
 * @param <O> output message
 *
 */
public interface MessageTranslator<I, O> {

    /**
     * Translates from input to output
     * @param input
     * @param deviceContext
     * @param connectionDistinguisher
     * @return message of output type
     */
    O translate(I input, DeviceContext deviceContext, Object connectionDistinguisher);

}
