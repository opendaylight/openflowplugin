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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.out._case.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCase;

public class OfToSalCopyTtlOutCase extends ConvertorCase<CopyTtlOutCase, Action, ActionResponseConvertorData> {
    public OfToSalCopyTtlOutCase() {
        super(CopyTtlOutCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<Action> process(@Nonnull final CopyTtlOutCase source, final ActionResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        CopyTtlOutBuilder copyTtlOutaction = new CopyTtlOutBuilder();
        return Optional.of(new CopyTtlOutCaseBuilder().setCopyTtlOut(copyTtlOutaction.build()).build());
    }
}
