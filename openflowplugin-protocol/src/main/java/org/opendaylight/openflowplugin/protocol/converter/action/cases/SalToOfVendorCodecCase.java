/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.action.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.protocol.converter.data.ActionConverterData;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalToOfVendorCodecCase extends ConvertorCase<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action, ActionConverterData> {
    private static final Logger LOG = LoggerFactory.getLogger(SalToOfVendorCodecCase.class);
    private final ExtensionConverterProvider extensionConverterProvider;

    public SalToOfVendorCodecCase(final ExtensionConverterProvider extensionConverterProvider) {
        super(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action source, final ActionConverterData data, ConverterExecutor converterExecutor) {
        final short version = data.getVersion();
        final TypeVersionKey<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> key =
                new TypeVersionKey<>(
                        (Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>) source.getImplementedInterface(),
                        version);

        if (extensionConverterProvider == null) {
            return Optional.empty();
        }

        final ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action> convertor =
                extensionConverterProvider.getConverter(key);

        LOG.trace("OFP Extension action, key:{}, converter:{}", key, convertor);
        return convertor != null ? Optional.of(convertor.convert(source)) : Optional.empty();
    }
}