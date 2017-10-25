/*
 * Copyright (c) 2016 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.NxActionLearn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.NxActionLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionLearnGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.FlowModSpec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromValueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromValueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModCopyFieldIntoFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModCopyFieldIntoFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModCopyValueIntoFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModCopyValueIntoFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModOutputToPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModOutputToPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.value._case.FlowModAddMatchFromValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.value._case.FlowModAddMatchFromValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.copy.field.into.field._case.FlowModCopyFieldIntoField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.copy.field.into.field._case.FlowModCopyFieldIntoFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.copy.value.into.field._case.FlowModCopyValueIntoField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.copy.value.into.field._case.FlowModCopyValueIntoFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.output.to.port._case.FlowModOutputToPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.output.to.port._case.FlowModOutputToPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.learn.grouping.NxLearn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.learn.grouping.NxLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.learn.grouping.nx.learn.FlowMods;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.learn.grouping.nx.learn.FlowModsBuilder;

public class LearnConvertorUtil {

    /*
     *                         CONVERT UP
    */

    static void convertUp(NxActionLearn action, NxLearnBuilder builder) {
        builder.setFlags(action.getFlags());
        builder.setCookie(action.getCookie());
        builder.setFinHardTimeout(action.getFinHardTimeout());
        builder.setFinIdleTimeout(action.getFinIdleTimeout());
        builder.setHardTimeout(action.getHardTimeout());
        builder.setIdleTimeout(action.getIdleTimeout());
        builder.setPriority(action.getPriority());
        builder.setTableId(action.getTableId());
        builder.setFlowMods(getFlowMods(action));
    }

    private static List<FlowMods> getFlowMods(NxActionLearn action) {
        if(action.getFlowMods() == null){
            return null;
        }

        List<FlowMods> flowMods = new ArrayList<FlowMods>();
        for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowMods flowMod : action.getFlowMods()){
            FlowModsBuilder flowModBuilder = new FlowModsBuilder();
            FlowModSpec flowModSpec = buildFlowModSpec(flowMod.getFlowModSpec());
            flowModBuilder.setFlowModSpec(flowModSpec);
            flowMods.add(flowModBuilder.build());
        }
        return flowMods;
    }


    private static FlowModSpec buildFlowModSpec(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.FlowModSpec flowModSpec) {
        if(flowModSpec instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCase){
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromField flowModAdd2 = 
                    ((org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCase) flowModSpec).getFlowModAddMatchFromField(); 
            FlowModAddMatchFromFieldBuilder flowModAdd = new FlowModAddMatchFromFieldBuilder();
            flowModAdd.setDstField(flowModAdd2.getDstField());
            flowModAdd.setSrcField(flowModAdd2.getSrcField());
            flowModAdd.setDstOfs(flowModAdd2.getDstOfs());
            flowModAdd.setSrcOfs(flowModAdd2.getSrcOfs());
            flowModAdd.setFlowModNumBits(flowModAdd2.getFlowModNumBits());
            FlowModAddMatchFromFieldCaseBuilder caseBuilder = new FlowModAddMatchFromFieldCaseBuilder();
            caseBuilder.setFlowModAddMatchFromField(flowModAdd.build());
            return caseBuilder.build();
        } else if(flowModSpec instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromValueCase){
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.value._case.FlowModAddMatchFromValue flowModAdd2 = 
                    ((org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromValueCase) flowModSpec).getFlowModAddMatchFromValue(); 
            FlowModAddMatchFromValueBuilder flowModAdd = new FlowModAddMatchFromValueBuilder();
            flowModAdd.setValue(flowModAdd2.getValue());
            flowModAdd.setSrcField(flowModAdd2.getSrcField());
            flowModAdd.setSrcOfs(flowModAdd2.getSrcOfs());
            flowModAdd.setFlowModNumBits(flowModAdd2.getFlowModNumBits());
            FlowModAddMatchFromValueCaseBuilder caseBuilder = new FlowModAddMatchFromValueCaseBuilder();
            caseBuilder.setFlowModAddMatchFromValue(flowModAdd.build());
            return caseBuilder.build();
        } else if(flowModSpec instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyFieldIntoFieldCase){
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.field.into.field._case.FlowModCopyFieldIntoField flowModCopy2 = 
                    ((org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyFieldIntoFieldCase) flowModSpec).getFlowModCopyFieldIntoField(); 
            FlowModCopyFieldIntoFieldBuilder flowModCopy = new FlowModCopyFieldIntoFieldBuilder();
            flowModCopy.setDstField(flowModCopy2.getDstField());
            flowModCopy.setDstOfs(flowModCopy2.getDstOfs());
            flowModCopy.setSrcField(flowModCopy2.getSrcField());
            flowModCopy.setSrcOfs(flowModCopy2.getSrcOfs());
            flowModCopy.setFlowModNumBits(flowModCopy2.getFlowModNumBits());
            FlowModCopyFieldIntoFieldCaseBuilder caseBuilder = new FlowModCopyFieldIntoFieldCaseBuilder();
            caseBuilder.setFlowModCopyFieldIntoField(flowModCopy.build());
            return caseBuilder.build();
        } else if(flowModSpec instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyValueIntoFieldCase){
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.value.into.field._case.FlowModCopyValueIntoField flowModCopy2 = 
                    ((org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyValueIntoFieldCase) flowModSpec).getFlowModCopyValueIntoField(); 
            FlowModCopyValueIntoFieldBuilder flowModCopy = new FlowModCopyValueIntoFieldBuilder();
            flowModCopy.setValue(flowModCopy2.getValue());
            flowModCopy.setDstField(flowModCopy2.getDstField());
            flowModCopy.setDstOfs(flowModCopy2.getDstOfs());
            flowModCopy.setFlowModNumBits(flowModCopy2.getFlowModNumBits());
            FlowModCopyValueIntoFieldCaseBuilder caseBuilder = new FlowModCopyValueIntoFieldCaseBuilder();
            caseBuilder.setFlowModCopyValueIntoField(flowModCopy.build());
            return caseBuilder.build();
        } else if(flowModSpec instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModOutputToPortCase){
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.output.to.port._case.FlowModOutputToPort flowModOut2 = 
                    ((org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModOutputToPortCase) flowModSpec).getFlowModOutputToPort(); 
            FlowModOutputToPortBuilder flowModOut = new FlowModOutputToPortBuilder();
            flowModOut.setSrcField(flowModOut2.getSrcField());
            flowModOut.setSrcOfs(flowModOut2.getSrcOfs());
            flowModOut.setFlowModNumBits(flowModOut2.getFlowModNumBits());
            FlowModOutputToPortCaseBuilder caseBuilder = new FlowModOutputToPortCaseBuilder();
            caseBuilder.setFlowModOutputToPort(flowModOut.build());
            return caseBuilder.build();
        }
        return null;
    }

    /*
     *                                  CONVERT DOWN
     */

    static void convertDown(NxActionLearnGrouping nxAction, NxActionLearnBuilder nxActionLearnBuilder) {
        nxActionLearnBuilder.setFlags(nxAction.getNxLearn().getFlags());
        nxActionLearnBuilder.setCookie(nxAction.getNxLearn().getCookie());
        nxActionLearnBuilder.setFinHardTimeout(nxAction.getNxLearn().getFinHardTimeout());
        nxActionLearnBuilder.setFinIdleTimeout(nxAction.getNxLearn().getFinIdleTimeout());
        nxActionLearnBuilder.setHardTimeout(nxAction.getNxLearn().getHardTimeout());
        nxActionLearnBuilder.setIdleTimeout(nxAction.getNxLearn().getIdleTimeout());
        nxActionLearnBuilder.setPriority(nxAction.getNxLearn().getPriority());
        nxActionLearnBuilder.setTableId(nxAction.getNxLearn().getTableId());
        nxActionLearnBuilder.setFlowMods(getFlowMods(nxAction.getNxLearn()));
    }

    private static List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowMods> getFlowMods(
            NxLearn nxLearn) {
        if(nxLearn.getFlowMods() == null){
            return null;
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowMods> flowMods = new ArrayList<>();
        for(FlowMods flowMod : nxLearn.getFlowMods()){
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowModsBuilder flowModBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowModsBuilder();
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.FlowModSpec flowModSpec = buildFlowModSpec(flowMod.getFlowModSpec());
            flowModBuilder.setFlowModSpec(flowModSpec);
            flowMods.add(flowModBuilder.build());
        }
        return flowMods;
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.FlowModSpec buildFlowModSpec(FlowModSpec flowModSpec) {
        if(flowModSpec instanceof FlowModAddMatchFromFieldCase){
            FlowModAddMatchFromField flowModAdd2 = ((FlowModAddMatchFromFieldCase) flowModSpec).getFlowModAddMatchFromField();
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromFieldBuilder 
                flowModAdd = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromFieldBuilder();
            flowModAdd.setDstField(flowModAdd2.getDstField());
            flowModAdd.setSrcField(flowModAdd2.getSrcField());
            flowModAdd.setDstOfs(flowModAdd2.getDstOfs());
            flowModAdd.setSrcOfs(flowModAdd2.getSrcOfs());
            flowModAdd.setFlowModNumBits(flowModAdd2.getFlowModNumBits());
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCaseBuilder 
                caseBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCaseBuilder();
            caseBuilder.setFlowModAddMatchFromField(flowModAdd.build());
            return caseBuilder.build();
        } else if(flowModSpec instanceof FlowModAddMatchFromValueCase){
            FlowModAddMatchFromValue flowModAdd2 = ((FlowModAddMatchFromValueCase) flowModSpec).getFlowModAddMatchFromValue();
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.value._case.FlowModAddMatchFromValueBuilder 
                flowModAdd = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.value._case.FlowModAddMatchFromValueBuilder();
            flowModAdd.setValue(flowModAdd2.getValue());
            flowModAdd.setSrcField(flowModAdd2.getSrcField());
            flowModAdd.setSrcOfs(flowModAdd2.getSrcOfs());
            flowModAdd.setFlowModNumBits(flowModAdd2.getFlowModNumBits());
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromValueCaseBuilder 
                caseBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromValueCaseBuilder();
            caseBuilder.setFlowModAddMatchFromValue(flowModAdd.build());
            return caseBuilder.build();
        } else if(flowModSpec instanceof FlowModCopyFieldIntoFieldCase){
            FlowModCopyFieldIntoField flowModCopy2 = ((FlowModCopyFieldIntoFieldCase) flowModSpec).getFlowModCopyFieldIntoField();
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.field.into.field._case.FlowModCopyFieldIntoFieldBuilder 
                flowModCopy = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.field.into.field._case.FlowModCopyFieldIntoFieldBuilder();
            flowModCopy.setDstField(flowModCopy2.getDstField());
            flowModCopy.setDstOfs(flowModCopy2.getDstOfs());
            flowModCopy.setSrcField(flowModCopy2.getSrcField());
            flowModCopy.setSrcOfs(flowModCopy2.getSrcOfs());
            flowModCopy.setFlowModNumBits(flowModCopy2.getFlowModNumBits());
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyFieldIntoFieldCaseBuilder 
                caseBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyFieldIntoFieldCaseBuilder();
            caseBuilder.setFlowModCopyFieldIntoField(flowModCopy.build());
            return caseBuilder.build();
        } else if (flowModSpec instanceof FlowModCopyValueIntoFieldCase) {
            FlowModCopyValueIntoField flowModCopy2 = ((FlowModCopyValueIntoFieldCase) flowModSpec).getFlowModCopyValueIntoField();
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.value.into.field._case.FlowModCopyValueIntoFieldBuilder 
                flowModCopy = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.value.into.field._case.FlowModCopyValueIntoFieldBuilder();
            flowModCopy.setDstField(flowModCopy2.getDstField());
            flowModCopy.setDstOfs(flowModCopy2.getDstOfs());
            flowModCopy.setValue(flowModCopy2.getValue());
            flowModCopy.setFlowModNumBits(flowModCopy2.getFlowModNumBits());
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyValueIntoFieldCaseBuilder 
                caseBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyValueIntoFieldCaseBuilder();
            caseBuilder.setFlowModCopyValueIntoField(flowModCopy.build());
            return caseBuilder.build();
        } else if (flowModSpec instanceof FlowModOutputToPortCase) {
            FlowModOutputToPort flowModOut2 = ((FlowModOutputToPortCase) flowModSpec).getFlowModOutputToPort();
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.output.to.port._case.FlowModOutputToPortBuilder 
                flowModOut = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.output.to.port._case.FlowModOutputToPortBuilder();
            flowModOut.setSrcField(flowModOut2.getSrcField());
            flowModOut.setSrcOfs(flowModOut2.getSrcOfs());
            flowModOut.setFlowModNumBits(flowModOut2.getFlowModNumBits());
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModOutputToPortCaseBuilder 
                caseBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModOutputToPortCaseBuilder();
            caseBuilder.setFlowModOutputToPort(flowModOut.build());
            return caseBuilder.build();
        }
        return null;
    }

}
