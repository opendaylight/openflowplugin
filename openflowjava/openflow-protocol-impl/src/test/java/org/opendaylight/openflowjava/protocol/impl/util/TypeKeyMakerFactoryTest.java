/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.InstructionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for TypeKeyMakerFactory.
 *
 * @author michal.polkorab
 */
public class TypeKeyMakerFactoryTest {

    /**
     * Tests {@link TypeKeyMakerFactory#createActionKeyMaker(short)}.
     */
    @Test
    public void testActionKeyMaker() {
        TypeKeyMaker<Action> keyMaker = TypeKeyMakerFactory.createActionKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null keyMaker", keyMaker);

        ActionBuilder builder = new ActionBuilder();
        builder.setActionChoice(new OutputActionCaseBuilder().build());
        Action action = builder.build();
        MessageTypeKey<?> key = keyMaker.make(action);

        Assert.assertNotNull("Null key", key);
        Assert.assertEquals("Wrong key", new ActionSerializerKey<>(EncodeConstants.OF13_VERSION_ID,
                        OutputActionCase.class, null), key);
    }

    /**
     * Tests {@link TypeKeyMakerFactory#createActionKeyMaker(short)}.
     */
    @Test
    public void testExperimenterActionKeyMaker() {
        TypeKeyMaker<Action> keyMaker = TypeKeyMakerFactory.createActionKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null keyMaker", keyMaker);

        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping
            .ActionBuilder builder = new ActionBuilder();
        builder.setExperimenterId(new ExperimenterId(Uint32.valueOf(42)));
        builder.setActionChoice(new CopyTtlInCaseBuilder().build());
        Action action = builder.build();
        MessageTypeKey<?> key = keyMaker.make(action);

        Assert.assertNotNull("Null key", key);
        Assert.assertEquals("Wrong key", new ActionSerializerKey<>(EncodeConstants.OF13_VERSION_ID,
                CopyTtlInCase.class, Uint32.valueOf(42)), key);
    }

    /**
     * Tests {@link TypeKeyMakerFactory#createInstructionKeyMaker(short)}.
     */
    @Test
    public void testInstructionKeyMaker() {
        TypeKeyMaker<Instruction> keyMaker =
                TypeKeyMakerFactory.createInstructionKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null keyMaker", keyMaker);

        InstructionBuilder builder = new InstructionBuilder();
        builder.setInstructionChoice(new GotoTableCaseBuilder().build());
        Instruction instruction = builder.build();
        MessageTypeKey<?> key = keyMaker.make(instruction);

        Assert.assertNotNull("Null key", key);
        Assert.assertEquals("Wrong key", new InstructionSerializerKey<>(EncodeConstants.OF13_VERSION_ID,
                        GotoTableCase.class, null), key);
    }

    /**
     * Tests {@link TypeKeyMakerFactory#createInstructionKeyMaker(short)}.
     */
    @Test
    public void testExperimenterInstructionKeyMaker() {
        TypeKeyMaker<Instruction> keyMaker =
                TypeKeyMakerFactory.createInstructionKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null keyMaker", keyMaker);

        InstructionBuilder builder = new InstructionBuilder();
        builder.setExperimenterId(new ExperimenterId(Uint32.valueOf(42)));
        builder.setInstructionChoice(new ClearActionsCaseBuilder().build());
        Instruction instruction = builder.build();
        MessageTypeKey<?> key = keyMaker.make(instruction);

        Assert.assertNotNull("Null key", key);
        Assert.assertEquals("Wrong key", new InstructionSerializerKey<>(EncodeConstants.OF13_VERSION_ID,
                        ClearActionsCase.class, 42L), key);
    }

    /**
     * Tests {@link TypeKeyMakerFactory#createMatchEntriesKeyMaker(short)}.
     */
    @Test
    public void testMatchEntriesKeyMaker() {
        TypeKeyMaker<MatchEntry> keyMaker =
                TypeKeyMakerFactory.createMatchEntriesKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null keyMaker", keyMaker);

        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(InPort.class);
        builder.setHasMask(true);
        MatchEntry entry = builder.build();
        MessageTypeKey<?> key = keyMaker.make(entry);

        Assert.assertNotNull("Null key", key);
        MatchEntrySerializerKey<?, ?> comparationKey = new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID,
                OpenflowBasicClass.class, InPort.class);
        Assert.assertEquals("Wrong key", comparationKey, key);
    }

    /**
     * Tests {@link TypeKeyMakerFactory#createMatchEntriesKeyMaker(short)}.
     */
    @Test
    public void testExperimenterMatchEntriesKeyMaker() {
        TypeKeyMaker<MatchEntry> keyMaker =
                TypeKeyMakerFactory.createMatchEntriesKeyMaker(EncodeConstants.OF13_VERSION_ID);
        Assert.assertNotNull("Null keyMaker", keyMaker);

        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(ExperimenterClass.class);
        builder.setOxmMatchField(OxmMatchFieldClass.class);
        builder.setHasMask(true);
        ExperimenterIdCaseBuilder caseBuilder = new ExperimenterIdCaseBuilder();
        ExperimenterBuilder expBuilder = new ExperimenterBuilder();
        expBuilder.setExperimenter(new ExperimenterId(Uint32.valueOf(42)));
        caseBuilder.setExperimenter(expBuilder.build());
        builder.setMatchEntryValue(caseBuilder.build());
        MatchEntry entry = builder.build();
        MessageTypeKey<?> key = keyMaker.make(entry);

        Assert.assertNotNull("Null key", key);
        MatchEntrySerializerKey<?, ?> comparationKey = new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID,
                ExperimenterClass.class, OxmMatchFieldClass.class);
        comparationKey.setExperimenterId(Uint32.valueOf(42L));
        Assert.assertEquals("Wrong key", comparationKey, key);
    }

    private interface OxmMatchFieldClass extends MatchField {
        // only for testing purposes
    }
}
