/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class AbstractExperimenterMultipartService<T extends OfHeader> extends AbstractMultipartService<SendExperimenterMpRequestInput, T> {

    private final ExtensionConverterProvider extensionConverterProvider;

    protected AbstractExperimenterMultipartService(RequestContextStack requestContextStack, DeviceContext deviceContext,
                                                   ExtensionConverterProvider extensionConverterProvider) {
        super(requestContextStack, deviceContext);
        this.extensionConverterProvider = extensionConverterProvider;
    }

    /**
     * Get extension converter provider
     * @return extension converter provider
     */
    protected ExtensionConverterProvider getExtensionConverterProvider() {
        return extensionConverterProvider;
    }

    /**
     * Process experimenter input and result experimenter output
     * @param input experimenter input
     * @return experimenter output
     */
    public abstract Future<RpcResult<SendExperimenterMpRequestOutput>> handleAndReply(SendExperimenterMpRequestInput input);

}
