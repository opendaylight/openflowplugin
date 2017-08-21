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
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.protocol.converter.data.ActionConverterData;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;

public class SalToOfGeneralExtensionGroupingCase extends ConvertorCase<GeneralExtensionGrouping, Action, ActionConverterData> {
    private final ExtensionConverterProvider extensionConverterProvider;

    public SalToOfGeneralExtensionGroupingCase(final ExtensionConverterProvider extensionConverterProvider) {
        super(GeneralExtensionGrouping.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final GeneralExtensionGrouping source, final ActionConverterData data, ConverterExecutor converterExecutor) {
        final short version = data.getVersion();
        /*
         * TODO: EXTENSION PROPOSAL (action, MD-SAL to OFJava)
         * - we might need sessionContext as converter input
         *
         */

        Extension extAction = source.getExtension();
        ConverterExtensionKey<? extends ExtensionKey> key = new ConverterExtensionKey<>(source.getExtensionKey(), version);
        ConvertorToOFJava<Action> convertor = extensionConverterProvider.getConverter(key);
        return convertor != null ? Optional.of(convertor.convert(extAction)) : Optional.empty();
    }
}