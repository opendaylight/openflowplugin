/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extensions.hpe;

import com.google.common.base.Optional;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.GroupingResolver;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchRpcUpdateFlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatchBuilder;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;

import java.util.HashSet;
import java.util.Set;

public abstract class HpeAbstractConvertor<T extends DataObject> implements ConvertorToOFJava<MatchEntry>,
        ConvertorFromOFJava<MatchEntry, MatchPath> {
    protected final Class<? extends ExtensionKey> key;
    protected final Class<? extends MatchField> oxmMatchField;
    protected final boolean hasMask;
    protected final GroupingResolver<T, Extension> groupingResolver;
    protected final ConverterExtensionKey<? extends ExtensionKey> converterExtensionKey;
    protected final MatchEntrySerializerKey<ExperimenterClass, ? extends MatchField> matchEntrySerializerKey;

    public Class<? extends MatchField> getOxmMatchField() {
        return oxmMatchField;
    }

    public ConverterExtensionKey<? extends ExtensionKey> getConverterExtensionKey() {
        return this.converterExtensionKey;
    }

    public MatchEntrySerializerKey<ExperimenterClass, ? extends MatchField> getMatchEntrySerializerKey() {
        return this.matchEntrySerializerKey;
    }

    protected HpeAbstractConvertor(Class<? extends ExtensionKey> key, Class<? extends MatchField> oxmMatchField, boolean hasMask, Class<T> groupingClass) {
        this.key = key;
        this.oxmMatchField = oxmMatchField;
        this.hasMask = hasMask;
        this.groupingResolver = new GroupingResolver<>(groupingClass);
        final Set<Class<? extends Augmentation<Extension>>> augmentationsOfExtension = new HashSet<>();
        augmentationsOfExtension.add(HpeAugMatchRpcAddFlow.class);
        augmentationsOfExtension.add(HpeAugMatchRpcRemoveFlow.class);
        augmentationsOfExtension.add(HpeAugMatchRpcUpdateFlowOriginal.class);
        augmentationsOfExtension.add(HpeAugMatchRpcUpdateFlowUpdated.class);
        augmentationsOfExtension.add(HpeAugMatchNodesNodeTableFlow.class);
        augmentationsOfExtension.add(HpeAugMatchNotifSwitchFlowRemoved.class);
        augmentationsOfExtension.add(HpeAugMatchNotifPacketIn.class);
        augmentationsOfExtension.add(HpeAugMatchNotifUpdateFlowStats.class);
        this.groupingResolver.setAugmentations(augmentationsOfExtension);
        this.converterExtensionKey = new ConverterExtensionKey<>(key, EncodeConstants.OF13_VERSION_ID);
        this.matchEntrySerializerKey = new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID,
                ExperimenterClass.class, oxmMatchField);
    }

    protected T getExtension(Extension extension) {
        Optional<T> matchGrouping = this.groupingResolver.getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new IllegalArgumentException("extension");
        }
        return matchGrouping.get();
    }

    @Override
    public MatchEntry convert(Extension extension) {
        ExperimenterIdCaseBuilder experimenterIdCaseBuilder = new ExperimenterIdCaseBuilder();
        ExperimenterBuilder experimenterBuilder = new ExperimenterBuilder();
        OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder = new OfjAugHpeMatchBuilder();
        convertToOFJava(extension, ofjAugHpeMatchBuilder);
        experimenterBuilder.setExperimenter(new ExperimenterId(HpeExtensionProviderImpl.HP_EXP_ID));
        experimenterBuilder.addAugmentation(OfjAugHpeMatch.class, ofjAugHpeMatchBuilder.build());
        experimenterIdCaseBuilder.setExperimenter(experimenterBuilder.build());
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setHasMask(this.hasMask);
        matchEntryBuilder.setOxmMatchField(this.oxmMatchField);
        matchEntryBuilder.setOxmClass(ExperimenterClass.class);
        matchEntryBuilder.setMatchEntryValue(experimenterIdCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(
            MatchEntry input, MatchPath path) {
        ExperimenterIdCase experimenterIdCase = ((ExperimenterIdCase) input.getMatchEntryValue());
        Experimenter experimenter = experimenterIdCase.getExperimenter();
        OfjAugHpeMatch ofjAugHpeMatch = experimenter.getAugmentation(OfjAugHpeMatch.class);
        return convertFromOFJava(path, ofjAugHpeMatch);
    }

    protected abstract ExtensionAugment<? extends Augmentation<Extension>> convertFromOFJava(MatchPath path, OfjAugHpeMatch ofjAugHpeMatch);

    protected abstract void convertToOFJava(Extension extension, OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder);
}
