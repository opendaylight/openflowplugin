/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.extension;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.AugmentTuple;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/19/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class MatchExtensionHelperTest {

    @Mock
    private ExtensionConverterProvider extensionConverterProvider;
    private static final int PRESET_COUNT = 7;
    private static final MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key = new MatchEntrySerializerKey<>(OpenflowVersion.OF13.getVersion(), MockOxmClassBase.class, MockMatchField.class);

    @Before
    public void setup() {
        OFSessionUtil.getSessionManager().setExtensionConverterProvider(extensionConverterProvider);
        when(extensionConverterProvider.getConverter(key)).thenReturn(new ConvertorFromOFJava<DataContainer, AugmentationPath>() {
            @Override
            public ExtensionAugment<? extends Augmentation<Extension>> convert(final DataContainer input, final AugmentationPath path) {
                MockAugmentation mockAugmentation = new MockAugmentation();
                return new ExtensionAugment<MockAugmentation>(MockAugmentation.class, mockAugmentation, MockExtensionKey.class);
            }
        });
    }


    @Test
    /**
     * Basic functionality test method for {@link org.opendaylight.openflowplugin.openflow.md.core.extension.MatchExtensionHelper#processAllExtensions(java.util.Collection, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion, org.opendaylight.openflowplugin.extension.api.path.MatchPath)}
     */
    public void testProcessAllExtensions() {

        List<MatchEntry> matchEntries = createMatchEntrieses();
        AugmentTuple augmentTuple = MatchExtensionHelper.processAllExtensions(matchEntries, OpenflowVersion.OF13, MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        assertNotNull(augmentTuple);

        augmentTuple = MatchExtensionHelper.processAllExtensions(matchEntries, OpenflowVersion.OF13, MatchPath.PACKETRECEIVED_MATCH);
        assertNotNull(augmentTuple);

        augmentTuple = MatchExtensionHelper.processAllExtensions(matchEntries, OpenflowVersion.OF13, MatchPath.SWITCHFLOWREMOVED_MATCH);
        assertNotNull(augmentTuple);
    }


    private static List<MatchEntry> createMatchEntrieses() {
        List<MatchEntry> matchEntries = new ArrayList<>();
        for (int i = 0; i < PRESET_COUNT; i++) {
            MatchEntryBuilder MatchEntryBuilder = new MatchEntryBuilder();
            MatchEntryBuilder.setHasMask(true);
            MatchEntryBuilder.setOxmClass(MockOxmClassBase.class);
            MatchEntryBuilder.setOxmMatchField(MockMatchField.class);
            matchEntries.add(MatchEntryBuilder.build());
        }
        return matchEntries;
    }

    private static final class MockOxmClassBase extends OxmClassBase {

    }

    private static final class MockMatchField extends MatchField {

    }

    private final class MockExtensionKey extends ExtensionKey {

    }

    private final class MockAugmentation implements Augmentation<Extension> {

    }
}
