/*
 * Copyright (c) 2017 ZTE Corporation.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddFlowWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddFlowWriteActionsSetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;

public class SetFieldExtensionTest {
    @Test
    public void testGetExtension() {
        GroupingLooseResolver<GeneralExtensionListGrouping> eqGroup =
                new GroupingLooseResolver<>(GeneralExtensionListGrouping.class);
        eqGroup.add(GeneralAugMatchRpcAddFlowWriteActionsSetField.class);
        eqGroup.add(GeneralAugMatchNodesNodeTableFlowWriteActionsSetField.class);

        ExtensionList extension1 = new ExtensionListBuilder().setExtensionKey(ZVendorExt1.class).build();
        SetField setField1 = new SetFieldBuilder()
                .addAugmentation(new GeneralAugMatchRpcAddFlowWriteActionsSetFieldBuilder()
                    .setExtensionList(Collections.singletonMap(extension1.key(), extension1))
                    .build())
                .build();

        ExtensionList extension2 = new ExtensionListBuilder().setExtensionKey(ZVendorExt2.class).build();
        SetField setField2 = new SetFieldBuilder()
                .addAugmentation(new GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder()
                    .setExtensionList(Collections.singletonMap(extension2.key(), extension2))
                    .build())
                .build();

        Assert.assertEquals(ZVendorExt1.class,
                eqGroup.getExtension(setField1).get().nonnullExtensionList().values().iterator().next()
                        .getExtensionKey());
        Assert.assertEquals(ZVendorExt2.class,
                eqGroup.getExtension(setField2).get().nonnullExtensionList().values().iterator().next()
                        .getExtensionKey());
    }

    private interface ZVendorExt1 extends ExtensionKey {
        // nobody
    }

    private interface ZVendorExt2 extends ExtensionKey {
        // nobody
    }
}
