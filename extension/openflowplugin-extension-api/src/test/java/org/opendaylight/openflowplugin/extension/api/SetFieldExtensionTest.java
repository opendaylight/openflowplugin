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
        GroupingLooseResolver<GeneralExtensionListGrouping> eqGroup = new GroupingLooseResolver<>(GeneralExtensionListGrouping.class);
        eqGroup.add(GeneralAugMatchRpcAddFlowWriteActionsSetField.class);
        eqGroup.add(GeneralAugMatchNodesNodeTableFlowWriteActionsSetField.class);

        SetFieldBuilder sb1 = new SetFieldBuilder();
        ExtensionList extension1 = new ExtensionListBuilder().setExtensionKey(ZVendorExt1.class).build();
        GeneralAugMatchRpcAddFlowWriteActionsSetField odlxxx1 = new GeneralAugMatchRpcAddFlowWriteActionsSetFieldBuilder().setExtensionList(Collections.singletonList(extension1)).build();
        SetField setField1 = sb1.addAugmentation(GeneralAugMatchRpcAddFlowWriteActionsSetField.class, odlxxx1).build();

        SetFieldBuilder sb2 = new SetFieldBuilder();
        ExtensionList extension2 = new ExtensionListBuilder().setExtensionKey(ZVendorExt2.class).build();
        GeneralAugMatchNodesNodeTableFlowWriteActionsSetField odlxxx2 = new GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder().setExtensionList(Collections.singletonList(extension2)).build();
        SetField setField2 = sb2.addAugmentation(GeneralAugMatchNodesNodeTableFlowWriteActionsSetField.class, odlxxx2).build();

        Assert.assertEquals(ZVendorExt1.class, eqGroup.getExtension(setField1).get().getExtensionList().get(0).getExtensionKey());
        Assert.assertEquals(ZVendorExt2.class, eqGroup.getExtension(setField2).get().getExtensionList().get(0).getExtensionKey());
    }

    private static class ZVendorExt1 extends ExtensionKey {
        // nobody
    }

    private static class ZVendorExt2 extends ExtensionKey {
        // nobody
    }
}
