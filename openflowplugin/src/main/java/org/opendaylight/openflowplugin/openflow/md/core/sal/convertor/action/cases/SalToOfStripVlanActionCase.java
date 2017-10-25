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
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.StripVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVidBuilder;

public class SalToOfStripVlanActionCase extends ConvertorCase<StripVlanActionCase, Action, ActionConvertorData> {
    public SalToOfStripVlanActionCase() {
        super(StripVlanActionCase.class, true, OFConstants.OFP_VERSION_1_3);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final StripVlanActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
        List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
        matchBuilder.setOxmClass(OpenflowBasicClass.class);
        matchBuilder.setOxmMatchField(VlanVid.class);
        matchBuilder.setHasMask(false);
        VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
        VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
        vlanVidBuilder.setCfiBit(true);
        vlanVidBuilder.setVlanVid(0x0000);
        vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
        matchBuilder.setMatchEntryValue(vlanVidCaseBuilder.build());
        matchBuilder.setHasMask(false);
        entries.add(matchBuilder.build());
        setFieldBuilder.setMatchEntry(entries);
        setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(setFieldCaseBuilder.build())
                .build());
    }
}
