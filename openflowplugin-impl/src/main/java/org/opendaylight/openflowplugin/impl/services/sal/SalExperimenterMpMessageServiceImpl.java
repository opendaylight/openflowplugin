/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerExperimenterMultipartService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerExperimenterMultipartService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SalExperimenterMpMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SalExperimenterMpMessageServiceImpl implements SalExperimenterMpMessageService {
    private final MultiLayerExperimenterMultipartService multiLayerService;
    private final SingleLayerExperimenterMultipartService singleLayerService;

    public SalExperimenterMpMessageServiceImpl(final RequestContextStack requestContextStack,
                                               final DeviceContext deviceContext,
                                               final ExtensionConverterProvider extensionConverterProvider) {
        this.singleLayerService = new SingleLayerExperimenterMultipartService(requestContextStack, deviceContext,
            extensionConverterProvider);
        this.multiLayerService = new MultiLayerExperimenterMultipartService(requestContextStack, deviceContext,
            extensionConverterProvider);
    }

    @Override
    public Future<RpcResult<SendExperimenterMpRequestOutput>> sendExperimenterMpRequest(SendExperimenterMpRequestInput input) {
        return singleLayerService.canUseSingleLayerSerialization()
            ? singleLayerService.handleAndReply(input)
            : multiLayerService.handleAndReply(input);
    }

}
