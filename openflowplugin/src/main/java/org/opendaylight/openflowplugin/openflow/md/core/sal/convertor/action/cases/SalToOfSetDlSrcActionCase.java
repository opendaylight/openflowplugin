/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.src._case.EthSrcBuilder;

public class SalToOfSetDlSrcActionCase extends ConvertorCase<SetDlSrcActionCase, Action, ActionConvertorData> {
    public SalToOfSetDlSrcActionCase() {
        super(SetDlSrcActionCase.class, true, OFConstants.OFP_VERSION_1_3);
    }

    @NonNull
    @Override
    public Optional<Action> process(@NonNull final SetDlSrcActionCase source, final ActionConvertorData data,
            ConvertorExecutor convertorExecutor) {
        SetDlSrcAction setdlsrcaction = source.getSetDlSrcAction();

        MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
        matchBuilder.setOxmClass(OpenflowBasicClass.VALUE);
        matchBuilder.setOxmMatchField(EthSrc.VALUE);
        EthSrcCaseBuilder ethSrcCaseBuilder = new EthSrcCaseBuilder();
        EthSrcBuilder ethSrcBuilder = new EthSrcBuilder();
        ethSrcBuilder.setMacAddress(setdlsrcaction.getAddress());
        matchBuilder.setHasMask(false);
        ethSrcCaseBuilder.setEthSrc(ethSrcBuilder.build());
        matchBuilder.setMatchEntryValue(ethSrcCaseBuilder.build());

        List<MatchEntry> entries = new ArrayList<>();
        entries.add(matchBuilder.build());

        SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
        setFieldBuilder.setMatchEntry(entries);

        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(setFieldCaseBuilder.build())
                .build());
    }
}
