/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import java.util.Objects;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.extension.api.ConverterMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.extension.api.exception.ConverterNotFoundException;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.experimenter._case.MultipartRequestExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class AbstractExperimenterMultipartService<T extends OfHeader> extends AbstractMultipartService<SendExperimenterMpRequestInput, T> {

    private final ExtensionConverterProvider extensionConverterProvider;

    protected AbstractExperimenterMultipartService(RequestContextStack requestContextStack, DeviceContext deviceContext,
                                                   ExtensionConverterProvider extensionConverterProvider) {
        super(requestContextStack, deviceContext);
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected OfHeader buildRequest(Xid xid, SendExperimenterMpRequestInput input) throws ServiceException {
        final TypeVersionKey key = new TypeVersionKey<>(
                input.getExperimenterMessageOfChoice().getImplementedInterface(),
                getVersion());

        final ConverterMessageToOFJava<ExperimenterMessageOfChoice, ExperimenterDataOfChoice> messageConverter =
                getExtensionConverterProvider().getMessageConverter(key);

        if (Objects.isNull(messageConverter)) {
            throw new ServiceException(new ConverterNotFoundException(key.toString()));
        }

        try {
            return RequestInputUtils
                .createMultipartHeader(MultipartType.OFPMPEXPERIMENTER, xid.getValue(), getVersion())
                .setMultipartRequestBody(new MultipartRequestExperimenterCaseBuilder()
                        .setMultipartRequestExperimenter(new MultipartRequestExperimenterBuilder()
                                .setExperimenter(messageConverter.getExperimenterId())
                                .setExpType(messageConverter.getType())
                                .setExperimenterDataOfChoice(messageConverter
                                        .convert(input.getExperimenterMessageOfChoice()))
                                .build())
                        .build())
                .build();
        } catch (final ConversionException e) {
            throw new ServiceException(e);
        }
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
