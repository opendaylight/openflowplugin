/*
 * Copyright (c) 2017 ZTE Corporation.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddFlowWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddFlowWriteActionsSetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;

public class SetFieldExtensionTest {
    @Test
    public void testGetExtension() {
        GroupingLooseResolver<GeneralExtensionListGrouping> eqGroup =
                new GroupingLooseResolver<>(GeneralExtensionListGrouping.class);
        eqGroup.add(GeneralAugMatchRpcAddFlowWriteActionsSetField.class);
        eqGroup.add(GeneralAugMatchNodesNodeTableFlowWriteActionsSetField.class);

        SetField setField1 = new SetFieldBuilder()
            .addAugmentation(new GeneralAugMatchRpcAddFlowWriteActionsSetFieldBuilder()
                .setExtensionList(BindingMap.of(new ExtensionListBuilder().setExtensionKey(ZVendorExt1.VALUE).build()))
                .build())
            .build();

        SetField setField2 = new SetFieldBuilder()
            .addAugmentation(new GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder()
                .setExtensionList(BindingMap.of(new ExtensionListBuilder().setExtensionKey(ZVendorExt2.VALUE).build()))
                .build())
            .build();

        assertEquals(ZVendorExt1.VALUE, eqGroup.getExtension(setField1).orElseThrow()
            .nonnullExtensionList().values().iterator().next().getExtensionKey());
        assertEquals(ZVendorExt2.VALUE, eqGroup.getExtension(setField2).orElseThrow()
            .nonnullExtensionList().values().iterator().next() .getExtensionKey());
    }

    private interface ZVendorExt1 extends ExtensionKey {
        ZVendorExt1 VALUE = () -> ZVendorExt1.class;
    }

    private interface ZVendorExt2 extends ExtensionKey {
        ZVendorExt2 VALUE = () -> ZVendorExt2.class;
    }
}
