/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.InstructionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.match.entry.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.InstructionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Creates KeyMakers.
 *
 * @author michal.polkorab
 */
public final class TypeKeyMakerFactory {

    private TypeKeyMakerFactory() {
        //not called
    }

    /**
     * Creates a key maker for MatchEntry instances.
     *
     * @param version openflow wire version that shall be used in lookup key
     * @return lookup key
     */
    public static TypeKeyMaker<MatchEntry> createMatchEntriesKeyMaker(final short version) {
        return new AbstractTypeKeyMaker<>(version) {
            @Override
            public MatchEntrySerializerKey<?, ?> make(final MatchEntry entry) {
                MatchEntrySerializerKey<?, ?> key;
                key = new MatchEntrySerializerKey<>(getVersion(), entry.getOxmClass(),
                        entry.getOxmMatchField());
                if (entry.getOxmClass().equals(ExperimenterClass.class)) {
                    ExperimenterIdCase entryValue = (ExperimenterIdCase) entry.getMatchEntryValue();
                    key.setExperimenterId(entryValue.getExperimenter().getExperimenter().getValue());
                    return key;
                }
                key.setExperimenterId(null);
                return key;
            }
        };
    }

    /**
     * Creates a key maker for Action instances.
     *
     * @param version openflow wire version that shall be used in lookup key
     * @return lookup key
     */
    public static TypeKeyMaker<Action> createActionKeyMaker(final short version) {
        return new AbstractTypeKeyMaker<>(version) {
            @Override
            public MessageTypeKey<?> make(final Action entry) {
                if (entry.getExperimenterId() != null) {
                    return new ActionSerializerKey<>(getVersion(),
                            (Class<ActionChoice>) entry.getActionChoice().implementedInterface(),
                            entry.getExperimenterId().getValue());
                }
                return new ActionSerializerKey<>(getVersion(),
                        (Class<ActionChoice>) entry.getActionChoice().implementedInterface(), (Uint32) null);
            }
        };
    }

    /**
     * Creates a key maker for Instruction instances.
     *
     * @param version openflow wire version that shall be used in lookup key
     * @return lookup key
     */
    public static TypeKeyMaker<Instruction> createInstructionKeyMaker(final short version) {
        return new AbstractTypeKeyMaker<>(version) {
            @Override
            public MessageTypeKey<?> make(final Instruction entry) {
                if (entry.getExperimenterId() != null) {
                    return new InstructionSerializerKey<>(getVersion(),
                            (Class<InstructionChoice>) entry.getInstructionChoice().implementedInterface(),
                            entry.getExperimenterId().getValue().toJava());
                }
                return new InstructionSerializerKey<>(getVersion(),
                        (Class<InstructionChoice>) entry.getInstructionChoice().implementedInterface(), null);
            }
        };
    }
}
