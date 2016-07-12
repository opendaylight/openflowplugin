/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalToOfGeneralExtensionGroupingCase extends ConvertorCase<GeneralExtensionGrouping, Action, ActionConvertorData> {
    private static final Logger LOG = LoggerFactory.getLogger(SalToOfGeneralExtensionGroupingCase.class);

    public SalToOfGeneralExtensionGroupingCase() {
        super(GeneralExtensionGrouping.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final GeneralExtensionGrouping source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        final short version = data.getVersion();
        /**
         * TODO: EXTENSION PROPOSAL (action, MD-SAL to OFJava)
         * - we might need sessionContext as converter input
         *
         */

        Extension extAction = source.getExtension();
        ConverterExtensionKey<? extends ExtensionKey> key = new ConverterExtensionKey<>(source.getExtensionKey(), version);
        ConvertorToOFJava<Action> convertor = OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
        return convertor != null ? Optional.of(convertor.convert(extAction)) : Optional.empty();
    }
}