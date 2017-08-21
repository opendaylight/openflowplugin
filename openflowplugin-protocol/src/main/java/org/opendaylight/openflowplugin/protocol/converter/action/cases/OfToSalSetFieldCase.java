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
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.protocol.converter.action.data.ActionResponseConverterData;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorCase;
import org.opendaylight.openflowplugin.protocol.converter.data.VersionDatapathIdConverterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldAction;

public class OfToSalSetFieldCase extends ConvertorCase<SetFieldCase, Action, ActionResponseConverterData> {
    public OfToSalSetFieldCase() {
        super(SetFieldCase.class, true, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<Action> process(@Nonnull final SetFieldCase source, final ActionResponseConverterData data, ConverterExecutor converterExecutor, final ExtensionConverterProvider extensionConverterProvider) {
        final VersionDatapathIdConverterData datapathIdConverterData = new VersionDatapathIdConverterData(data.getVersion());
        final SetFieldAction setFieldAction = source.getSetFieldAction();
        final SetFieldBuilder setField = new SetFieldBuilder();
        final Optional<MatchBuilder> matchOptional = converterExecutor.convert(setFieldAction, datapathIdConverterData);
        setField.fieldsFrom(matchOptional.orElse(new MatchBuilder()).build());

        return Optional.of(new SetFieldCaseBuilder().setSetField(setField.build()).build());
    }
}
