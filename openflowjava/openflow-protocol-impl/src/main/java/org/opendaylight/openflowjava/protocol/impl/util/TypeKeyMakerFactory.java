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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.InstructionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;

/**
 * Creates KeyMakers
 * @author michal.polkorab
 */
public abstract class TypeKeyMakerFactory {

    private TypeKeyMakerFactory() {
        //not called
    }

    /**
     * @param version openflow wire version that shall be used
     *  in lookup key
     * @return lookup key
     */
    public static TypeKeyMaker<MatchEntry> createMatchEntriesKeyMaker(short version) {
        return new AbstractTypeKeyMaker<MatchEntry>(version) {
            @Override
            public MatchEntrySerializerKey<?, ?> make(MatchEntry entry) {
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
     * @param version openflow wire version that shall be used
     *  in lookup key
     * @return lookup key
     */
    public static TypeKeyMaker<Action> createActionKeyMaker(short version) {
        return new AbstractTypeKeyMaker<Action>(version) {
            @Override
            public MessageTypeKey<?> make(Action entry) {
                if (entry.getExperimenterId() != null) {
                    return new ActionSerializerKey<>(getVersion(),
                            (Class<ActionChoice>) entry.getActionChoice().getImplementedInterface(),
                            entry.getExperimenterId().getValue());
                }
                return new ActionSerializerKey<>(getVersion(),
                        (Class<ActionChoice>) entry.getActionChoice().getImplementedInterface(), null);
            }
        };
    }

    /**
     * @param version openflow wire version that shall be used
     *  in lookup key
     * @return lookup key
     */
    public static TypeKeyMaker<Instruction> createInstructionKeyMaker(short version) {
        return new AbstractTypeKeyMaker<Instruction>(version) {
            @Override
            public MessageTypeKey<?> make(Instruction entry) {
                if (entry.getExperimenterId() != null) {
                    return new InstructionSerializerKey<>(getVersion(),
                            (Class<InstructionChoice>) entry.getInstructionChoice().getImplementedInterface(),
                            entry.getExperimenterId().getValue());
                }
                return new InstructionSerializerKey<>(getVersion(),
                        (Class<InstructionChoice>) entry.getInstructionChoice().getImplementedInterface(), null);
            }
        };
    }
}
