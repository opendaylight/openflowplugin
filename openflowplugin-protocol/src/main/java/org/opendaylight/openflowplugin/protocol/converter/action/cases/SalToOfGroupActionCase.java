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
import org.opendaylight.openflowplugin.protocol.converter.action.data.ActionConverterData;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.group._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

public class SalToOfGroupActionCase extends ConvertorCase<GroupActionCase, Action, ActionConverterData> {
    public SalToOfGroupActionCase() {
        super(GroupActionCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final GroupActionCase source, final ActionConverterData data, ConverterExecutor converterExecutor) {
        GroupAction groupAction = source.getGroupAction();
        GroupActionBuilder groupActionBuilder = new GroupActionBuilder();

        if (null != groupAction.getGroupId()) {
            groupActionBuilder.setGroupId(groupAction.getGroupId());
        } else {
            groupActionBuilder.setGroupId(Long.parseLong(groupAction.getGroup()));
        }

        return Optional.of(new ActionBuilder()
                .setActionChoice(new GroupCaseBuilder()
                        .setGroupAction(groupActionBuilder.build())
                        .build())
                .build());
    }
}
