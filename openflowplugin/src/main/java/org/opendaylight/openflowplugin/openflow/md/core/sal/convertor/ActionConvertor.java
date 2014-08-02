/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: hema.gopalkrishnan@ericsson.com
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ActionExtensionHelper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionSetNwDstReactor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionSetNwSrcReactor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanPcpActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.StripVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.out._case.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.mpls.ttl._case.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.pbb.action._case.PopPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.pcp.action._case.SetVlanPcpAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.CommonPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.DlAddressAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.DlAddressActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaxLengthAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaxLengthActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NwTosAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NwTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmFieldsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmFieldsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.QueueIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.QueueIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author usha@ericsson Action List:This class takes data from SAL layer and
 *         converts into OF Data
 * @author avishnoi@in.ibm.com Added convertor for OF bucket actions to SAL
 *         actions
 */
public final class ActionConvertor {
    private static final Logger logger = LoggerFactory.getLogger(ActionConvertor.class);

    private ActionConvertor() {
        // NOOP
    }

    /**
     * Translates SAL actions into OF Library actions
     *
     * @param actions    SAL actions
     * @param version    Openflow protocol version used
     * @param datapathid
     * @return OF Library actions
     */
    public static List<Action> getActions(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions,
            short version, BigInteger datapathid)

    {
        List<Action> actionsList = new ArrayList<>();
        Action ofAction;

        for (int actionItem = 0; actionItem < actions.size(); actionItem++) {
            ofAction = null;
            ActionBuilder actionBuilder = new ActionBuilder();

            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action = actions.get(
                    actionItem).getAction();

            if (action instanceof OutputActionCase)
                ofAction = salToOFOutputAction(action, actionBuilder, version);
            else if (action instanceof GroupActionCase)
                ofAction = SalToOFGroupAction(action, actionBuilder);
            else if (action instanceof CopyTtlOutCase)
                ofAction = SalToOFCopyTTLIOut(actionBuilder);
            else if (action instanceof CopyTtlInCase)
                ofAction = SalToOFCopyTTLIIn(actionBuilder);
            else if (action instanceof SetMplsTtlActionCase)
                ofAction = SalToOFSetMplsTtl(action, actionBuilder);
            else if (action instanceof DecMplsTtlCase)
                ofAction = SalToOFDecMplsTtl(actionBuilder);
            else if (action instanceof PushVlanActionCase)
                ofAction = SalToOFPushVlanAction(action, actionBuilder);
            else if (action instanceof PopVlanActionCase)
                ofAction = (version == OFConstants.OFP_VERSION_1_0) ? SalToOFStripVlan(actionBuilder, version)
                        : SalToOFPopVlan(actionBuilder);
            else if (action instanceof PushMplsActionCase)
                ofAction = SalToOFPushMplsAction(action, actionBuilder);
            else if (action instanceof PopMplsActionCase)
                ofAction = SalToOFPopMpls(action, actionBuilder);
            else if (action instanceof SetQueueActionCase)
                ofAction = SalToOFSetQueue(action, actionBuilder);
            else if (action instanceof SetNwTtlActionCase)
                ofAction = SalToOFSetNwTtl(action, actionBuilder);
            else if (action instanceof DecNwTtlCase)
                ofAction = SalToOFDecNwTtl(actionBuilder);
            else if (action instanceof SetFieldCase)
                ofAction = SalToOFSetField(action, actionBuilder, version, datapathid);
            else if (action instanceof PushPbbActionCase)
                ofAction = SalToOFPushPbbAction(action, actionBuilder);
            else if (action instanceof PopPbbActionCase)
                ofAction = SalToOFPopPBB(actionBuilder);

                // 1.0 Actions
            else if (action instanceof SetVlanIdActionCase)
                ofAction = SalToOFSetVlanId(action, actionBuilder, version);
            else if (action instanceof SetVlanPcpActionCase)
                ofAction = SalToOFSetVlanpcp(action, actionBuilder, version);
            else if (action instanceof StripVlanActionCase)
                ofAction = SalToOFStripVlan(actionBuilder, version);
            else if (action instanceof SetDlSrcActionCase)
                ofAction = SalToOFSetDlSrc(action, actionBuilder, version);
            else if (action instanceof SetDlDstActionCase)
                ofAction = SalToOFSetDlDst(action, actionBuilder, version);
            else if (action instanceof SetNwSrcActionCase)
                ofAction = SalToOFSetNwSrc(action, actionBuilder, version);
            else if (action instanceof SetNwDstActionCase)
                ofAction = SalToOFSetNwDst(action, actionBuilder, version);
            else if (action instanceof SetTpSrcActionCase)
                ofAction = SalToOFSetTpSrc(action, actionBuilder, version);
            else if (action instanceof SetTpDstActionCase)
                ofAction = SalToOFSetTpDst(action, actionBuilder, version);
            else if (action instanceof SetNwTosActionCase)
                ofAction = SalToOFSetNwTos(action, actionBuilder, version);
            else if (action instanceof GeneralExtensionGrouping) {
                
                /**
                 * TODO: EXTENSION PROPOSAL (action, MD-SAL to OFJava)
                 * - we might need sessionContext as converter input
                 * 
                 */
                
                GeneralExtensionGrouping extensionCaseGrouping = (GeneralExtensionGrouping) action;
                Extension extAction = extensionCaseGrouping.getExtension();
                ConverterExtensionKey<? extends ExtensionKey> key = new ConverterExtensionKey<>(extensionCaseGrouping.getExtensionKey(), version);
                ConvertorToOFJava<Action> convertor = 
                        OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
                if (convertor != null) {
                    ofAction = convertor.convert(extAction);
                }
            } else {
                // try vendor codecs
                TypeVersionKey<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> key =
                        new TypeVersionKey<>(
                                (Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>) action.getImplementedInterface(),
                                version);
                ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action> convertor = 
                        OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
                if (convertor != null) {
                    ofAction = convertor.convert(action);
                }
            }
            
            if (ofAction != null) {
                actionsList.add(ofAction);
            }
        }
        return actionsList;
    }

    private static Action SalToOFSetField(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version, BigInteger datapathid) {

        SetFieldCase setFieldCase = (SetFieldCase) action;
        org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match match = setFieldCase
                .getSetField();

        OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
        MatchReactor.getInstance().convert(match, version, oxmFieldsActionBuilder, datapathid);

        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);

        actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
        return actionBuilder.build();
    }

    private static Action SalToOFDecNwTtl(ActionBuilder actionBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl.class);
        return emtpyAction(actionBuilder);
    }

    private static Action SalToOFPushMplsAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        PushMplsActionCase pushMplsActionCase = (PushMplsActionCase) action;
        actionBuilder.setType(PushMpls.class);

        return SalToOFPushAction(pushMplsActionCase.getPushMplsAction().getEthernetType(), actionBuilder);
    }

    private static Action SalToOFPushPbbAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        PushPbbActionCase pushPbbActionCase = (PushPbbActionCase) action;
        actionBuilder.setType(PushPbb.class);

        return SalToOFPushAction(pushPbbActionCase.getPushPbbAction().getEthernetType(), actionBuilder);
    }

    private static Action SalToOFPushVlanAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        PushVlanActionCase pushVlanActionCase = (PushVlanActionCase) action;
        PushVlanAction pushVlanAction = pushVlanActionCase.getPushVlanAction();
        actionBuilder.setType(PushVlan.class);

        return SalToOFPushAction(pushVlanAction.getEthernetType(), actionBuilder);
    }

    private static Action SalToOFSetNwTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        SetNwTtlActionCase nwTtlActionCase = (SetNwTtlActionCase) action;
        NwTtlActionBuilder nwTtlActionBuilder = new NwTtlActionBuilder();
        nwTtlActionBuilder.setNwTtl(nwTtlActionCase.getSetNwTtlAction().getNwTtl());
        actionBuilder.setType(SetNwTtl.class);
        actionBuilder.addAugmentation(NwTtlAction.class, nwTtlActionBuilder.build());
        return actionBuilder.build();
    }

    private static Action SalToOFSetQueue(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        SetQueueActionCase setQueueActionCase = (SetQueueActionCase) action;
        SetQueueAction setQueueAction = setQueueActionCase.getSetQueueAction();

        QueueIdActionBuilder queueIdActionBuilder = new QueueIdActionBuilder();
        queueIdActionBuilder.setQueueId(setQueueAction.getQueueId());
        actionBuilder.setType(SetQueue.class);
        actionBuilder.addAugmentation(QueueIdAction.class, queueIdActionBuilder.build());

        return actionBuilder.build();
    }

    private static Action SalToOFPopMpls(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        PopMplsActionCase popMplsActionCase = (PopMplsActionCase) action;
        actionBuilder.setType(PopMpls.class);

        return SalToOFPushAction(popMplsActionCase.getPopMplsAction().getEthernetType(), actionBuilder);
    }

    private static Action SalToOFPopVlan(ActionBuilder actionBuilder) {
        actionBuilder.setType(PopVlan.class);
        return emtpyAction(actionBuilder);
    }

    private static Action SalToOFPopPBB(ActionBuilder actionBuilder) {
        actionBuilder.setType(PopPbb.class);
        return emtpyAction(actionBuilder);
    }

    private static Action SalToOFSetVlanId(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        SetVlanIdActionCase setvlanidcase = (SetVlanIdActionCase) action;
        SetVlanIdAction setvlanidaction = setvlanidcase.getSetVlanIdAction();

        if (version == OFConstants.OFP_VERSION_1_0) {

            VlanVidActionBuilder vlanidActionBuilder = new VlanVidActionBuilder();
            vlanidActionBuilder.setVlanVid(setvlanidaction.getVlanId().getValue());
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetVlanVid.class);
            actionBuilder.addAugmentation(VlanVidAction.class, vlanidActionBuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntries> matchEntriesList = new ArrayList<>();
            MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setOxmMatchField(VlanVid.class);
            VlanVidMatchEntryBuilder vlanVidBuilder = new VlanVidMatchEntryBuilder();
            vlanVidBuilder.setCfiBit(true);
            vlanVidBuilder.setVlanVid(setvlanidaction.getVlanId().getValue());
            matchEntriesBuilder.addAugmentation(VlanVidMatchEntry.class, vlanVidBuilder.build());
            matchEntriesBuilder.setHasMask(false);
            matchEntriesList.add(matchEntriesBuilder.build());
            oxmFieldsActionBuilder.setMatchEntries(matchEntriesList);
            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            logger.error("Unknown Action Type for the Version", version);
            return null;
        }
    }

    private static Action SalToOFSetVlanpcp(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        SetVlanPcpActionCase setvlanpcpcase = (SetVlanPcpActionCase) action;
        SetVlanPcpAction setvlanpcpaction = setvlanpcpcase.getSetVlanPcpAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            VlanPcpActionBuilder vlanpcpActionBuilder = new VlanPcpActionBuilder();
            vlanpcpActionBuilder.setVlanPcp(setvlanpcpaction.getVlanPcp().getValue());
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetVlanPcp.class);
            actionBuilder.addAugmentation(VlanPcpAction.class, vlanpcpActionBuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntries> matchEntriesList = new ArrayList<>();
            matchEntriesList.add(MatchConvertorImpl.toOfVlanPcp(setvlanpcpaction.getVlanPcp()));
            oxmFieldsActionBuilder.setMatchEntries(matchEntriesList);
            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            logger.error("Unknown Action Type for the Version", version);
            return null;
        }
    }

    private static Action SalToOFStripVlan(ActionBuilder actionBuilder, short version) {
        if (version == OFConstants.OFP_VERSION_1_0) {
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.StripVlan.class);

            return emtpyAction(actionBuilder);
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntries> matchEntriesList = new ArrayList<>();
            MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setOxmMatchField(VlanVid.class);
            VlanVidMatchEntryBuilder vlanVidBuilder = new VlanVidMatchEntryBuilder();
            vlanVidBuilder.setCfiBit(true);
            vlanVidBuilder.setVlanVid(new Integer(0x0000));
            matchEntriesBuilder.addAugmentation(VlanVidMatchEntry.class, vlanVidBuilder.build());
            matchEntriesBuilder.setHasMask(false);
            matchEntriesList.add(matchEntriesBuilder.build());
            oxmFieldsActionBuilder.setMatchEntries(matchEntriesList);
            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            logger.error("Unknown Action Type for the Version", version);
            return null;
        }
    }

    private static Action SalToOFSetDlSrc(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        SetDlSrcActionCase setdlsrccase = (SetDlSrcActionCase) action;
        SetDlSrcAction setdlsrcaction = setdlsrccase.getSetDlSrcAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            DlAddressActionBuilder dladdressactionbuilder = new DlAddressActionBuilder();
            dladdressactionbuilder.setDlAddress(setdlsrcaction.getAddress());
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetDlSrc.class);
            actionBuilder.addAugmentation(DlAddressAction.class, dladdressactionbuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntries> matchEntriesList = new ArrayList<>();
            matchEntriesList.add(MatchConvertorImpl.toOfMacAddress(EthSrc.class, setdlsrcaction.getAddress(), null));
            oxmFieldsActionBuilder.setMatchEntries(matchEntriesList);
            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            logger.error("Unknown Action Type for the Version", version);
            return null;
        }

    }

    private static Action SalToOFSetDlDst(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        SetDlDstActionCase setdldstcase = (SetDlDstActionCase) action;
        SetDlDstAction setdldstaction = setdldstcase.getSetDlDstAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            DlAddressActionBuilder dladdressactionbuilder = new DlAddressActionBuilder();
            dladdressactionbuilder.setDlAddress(setdldstaction.getAddress());
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetDlDst.class);
            actionBuilder.addAugmentation(DlAddressAction.class, dladdressactionbuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntries> matchEntriesList = new ArrayList<>();
            matchEntriesList.add(MatchConvertorImpl.toOfMacAddress(EthDst.class, setdldstaction.getAddress(), null));
            oxmFieldsActionBuilder.setMatchEntries(matchEntriesList);
            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            logger.error("Unknown Action Type for the Version", version);
            return null;
        }

    }

    protected static Action SalToOFSetNwSrc(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        try {
            ActionSetNwSrcReactor.getInstance().convert((SetNwSrcActionCase) action, version, actionBuilder, null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        return actionBuilder.build();
    }

    protected static Action SalToOFSetNwDst(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        try {
            ActionSetNwDstReactor.getInstance().convert((SetNwDstActionCase) action, version, actionBuilder, null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        return actionBuilder.build();
    }

    private static Action SalToOFSetNwTos(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        SetNwTosActionCase setnwtoscase = (SetNwTosActionCase) action;
        SetNwTosAction setnwtosaction = setnwtoscase.getSetNwTosAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            NwTosActionBuilder tosBuilder = new NwTosActionBuilder();
            tosBuilder.setNwTos(setnwtosaction.getTos().shortValue());
            actionBuilder.addAugmentation(NwTosAction.class, tosBuilder.build());
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTos.class);

            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntries> matchEntriesList = new ArrayList<>();
            matchEntriesList.add(MatchConvertorImpl.toOfIpDscp(new Dscp(setnwtosaction.getTos().shortValue())));
            oxmFieldsActionBuilder.setMatchEntries(matchEntriesList);
            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            logger.error("Unknown Action Type for the Version", version);
            return null;
        }

    }

    private static Action SalToOFSetTpSrc(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        if (version == OFConstants.OFP_VERSION_1_0) {
            SetTpSrcActionCase settpsrccase = (SetTpSrcActionCase) action;
            SetTpSrcAction settpsrcaction = settpsrccase.getSetTpSrcAction();

            PortActionBuilder settpsrc = new PortActionBuilder();
            PortNumber port = new PortNumber(settpsrcaction.getPort().getValue().longValue());
            settpsrc.setPort(port);

            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetTpSrc.class);
            actionBuilder.addAugmentation(PortAction.class, settpsrc.build());
            return actionBuilder.build();
        }
        logger.error("Unknown Action Type for the Version", version);
        return null;
    }

    private static Action SalToOFSetTpDst(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        if (version == OFConstants.OFP_VERSION_1_0) {
            SetTpDstActionCase settpdstcase = (SetTpDstActionCase) action;
            SetTpDstAction settpdstaction = settpdstcase.getSetTpDstAction();
            PortActionBuilder settpdst = new PortActionBuilder();
            PortNumber port = new PortNumber(settpdstaction.getPort().getValue().longValue());
            settpdst.setPort(port);

            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetTpDst.class);
            actionBuilder.addAugmentation(PortAction.class, settpdst.build());
            return actionBuilder.build();
        }
        logger.error("Unknown Action Type for the Version", version);
        return null;
    }

    private static Action SalToOFGroupAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {

        GroupActionCase groupActionCase = (GroupActionCase) action;
        GroupAction groupAction = groupActionCase.getGroupAction();

        GroupIdActionBuilder groupIdBuilder = new GroupIdActionBuilder();
        groupIdBuilder.setGroupId(groupAction.getGroupId());
        actionBuilder.setType(Group.class);
        actionBuilder.addAugmentation(GroupIdAction.class, groupIdBuilder.build());
        return actionBuilder.build();
    }

    private static Action SalToOFPushAction(Integer ethernetType, ActionBuilder actionBuilder) {
        EthertypeActionBuilder ethertypeActionBuilder = new EthertypeActionBuilder();
        if (ethernetType != null) {
            ethertypeActionBuilder.setEthertype(new EtherType(ethernetType));
        }

        /* OF */
        actionBuilder.addAugmentation(EthertypeAction.class, ethertypeActionBuilder.build());
        return actionBuilder.build();
    }

    private static Action SalToOFDecMplsTtl(ActionBuilder actionBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class);
        return emtpyAction(actionBuilder);
    }

    private static Action SalToOFSetMplsTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        SetMplsTtlActionCase mplsTtlActionCase = (SetMplsTtlActionCase) action;
        SetMplsTtlAction mplsTtlAction = mplsTtlActionCase.getSetMplsTtlAction();

        MplsTtlActionBuilder mplsTtlActionBuilder = new MplsTtlActionBuilder();
        mplsTtlActionBuilder.setMplsTtl(mplsTtlAction.getMplsTtl()/* SAL */);
        /* OF */
        actionBuilder.setType(SetMplsTtl.class);
        actionBuilder.addAugmentation(MplsTtlAction.class, mplsTtlActionBuilder.build());
        return actionBuilder.build();
    }

    private static Action SalToOFCopyTTLIIn(ActionBuilder actionBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class);
        return emtpyAction(actionBuilder);
    }

    private static Action SalToOFCopyTTLIOut(ActionBuilder actionBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class);
        return emtpyAction(actionBuilder);

    }

    private static Action emtpyAction(ActionBuilder actionBuilder) {
        return actionBuilder.build();
    }

    private static Action salToOFOutputAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        OutputActionCase outputActionCase = ((OutputActionCase) action);
        OutputAction outputAction = outputActionCase.getOutputAction();
        PortActionBuilder portAction = new PortActionBuilder();
        MaxLengthActionBuilder maxLenActionBuilder = new MaxLengthActionBuilder();
        if (outputAction.getMaxLength() != null) {
            maxLenActionBuilder.setMaxLength(outputAction.getMaxLength());
        } else {
            maxLenActionBuilder.setMaxLength(new Integer(0));
        }
        actionBuilder.addAugmentation(MaxLengthAction.class, maxLenActionBuilder.build());

        Uri uri = outputAction.getOutputNodeConnector();

        OpenflowVersion ofVersion = OpenflowVersion.get(version);
        Long portNumber = InventoryDataServiceUtil.portNumberfromNodeConnectorId(ofVersion, uri.getValue());
        if (OpenflowPortsUtil.checkPortValidity(ofVersion, portNumber)) {
            portAction.setPort(new PortNumber(portNumber));
        } else {
            logger.error("Invalid Port specified " + portNumber + " for Output Action for OF version:" + ofVersion);
        }

        actionBuilder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class);
        actionBuilder.addAugmentation(PortAction.class, portAction.build());
        return actionBuilder.build();

    }

    /**
     * Method to convert OF actions associated with bucket to SAL Actions.
     *
     * @param actionList
     * @param ofVersion  current ofp version
     * @param actionPath TODO
     * @return List of converted SAL Actions.
     */
    public static List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> toMDSalActions(
            List<Action> actionList, OpenflowVersion ofVersion, ActionPath actionPath) {

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> bucketActions = new ArrayList<>();
        for (Action action : actionList) {
            if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class)) {
                bucketActions.add(ofToSALOutputAction(ofVersion, action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group.class)) {
                bucketActions.add(ofToSALGroupAction(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class)) {
                CopyTtlOutBuilder copyTtlOutaction = new CopyTtlOutBuilder();
                bucketActions.add(new CopyTtlOutCaseBuilder().setCopyTtlOut(copyTtlOutaction.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class)) {
                CopyTtlInBuilder copyTtlInaction = new CopyTtlInBuilder();
                bucketActions.add(new CopyTtlInCaseBuilder().setCopyTtlIn(copyTtlInaction.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl.class)) {
                bucketActions.add(ofToSALSetMplsTtl(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class)) {
                DecMplsTtlBuilder decMplsTtl = new DecMplsTtlBuilder();
                bucketActions.add(new DecMplsTtlCaseBuilder().setDecMplsTtl(decMplsTtl.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan.class)) {
                bucketActions.add(ofToSALPushVlanAction(action));

            } else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan.class)
                    || action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.StripVlan.class)) {
                // OF1.0 nodes will emit StripVlan and OF1.3+ will emit StripVlan/PopVlan, convert both to PopVlan for SAL
                PopVlanActionBuilder popVlan = new PopVlanActionBuilder();
                bucketActions.add(new PopVlanActionCaseBuilder().setPopVlanAction(popVlan.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls.class)) {
                bucketActions.add(ofToSALPushMplsAction(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls.class)) {
                bucketActions.add(ofToSALPopMplsAction(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue.class)) {
                bucketActions.add(ofToSALSetQueue(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl.class)) {
                bucketActions.add(ofToSALSetNwTtl(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl.class)) {
                DecNwTtlBuilder decNwTtl = new DecNwTtlBuilder();
                bucketActions.add(new DecNwTtlCaseBuilder().setDecNwTtl(decNwTtl.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class)) {
                bucketActions.add(new SetFieldCaseBuilder().setSetField(MatchConvertorImpl.fromOFSetFieldToSALSetFieldAction(action, ofVersion))
                        .build());
            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb.class)) {
                bucketActions.add(ofToSALPushPbbAction(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb.class)) {
                PopPbbActionBuilder popPbb = new PopPbbActionBuilder();
                bucketActions.add(new PopPbbActionCaseBuilder().setPopPbbAction(popPbb.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter.class)) {
                /**
                 * TODO: EXTENSION PROPOSAL (action, OFJava to MD-SAL)
                 * - we might also need a way on how to identify exact type of augmentation to be 
                 *   used as match can be bound to multiple models
                 */
                org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action processedAction = 
                        ActionExtensionHelper.processAlienAction(action, ofVersion, actionPath);
                if (processedAction != null) {
                    bucketActions.add(processedAction);
                }
            }
        }
        return bucketActions;
    }

    /**
     * Method converts OF Output action object to SAL Output action object.
     *
     * @param ofVersion
     * @param ofVersion
     * @param action    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.
     *                  action.rev130731.actions.actions.list.Action
     * @return OutputAction
     */
    public static OutputActionCase ofToSALOutputAction(OpenflowVersion ofVersion, Action action) {
        OutputActionBuilder outputAction = new OutputActionBuilder();
        PortAction port = action.getAugmentation(PortAction.class);
        if (port != null) {
            CommonPort.PortNumber protocolAgnosticPort = OpenflowPortsUtil.getProtocolAgnosticPort(
                    ofVersion, port.getPort().getValue());
            String portNumberAsString = OpenflowPortsUtil.portNumberToString(protocolAgnosticPort);
            outputAction.setOutputNodeConnector(new Uri(portNumberAsString));
        } else {
            logger.error("Provided action is not OF Output action, no associated port found!");
        }

        MaxLengthAction length = action.getAugmentation(MaxLengthAction.class);
        if (length != null) {
            outputAction.setMaxLength(length.getMaxLength());
        } else {
            logger.error("Provided action is not OF Output action, no associated length found!");
        }

        return new OutputActionCaseBuilder().setOutputAction(outputAction.build()).build();
    }

    /**
     * Method converts OF GroupAction object to SAL GroupAction object
     *
     * @param action
     * @return GroupAction
     */
    public static GroupActionCase ofToSALGroupAction(Action action) {

        GroupActionBuilder groupAction = new GroupActionBuilder();

        GroupIdAction groupId = action.getAugmentation(GroupIdAction.class);
        groupAction.setGroupId(groupId.getGroupId());

        return new GroupActionCaseBuilder().setGroupAction(groupAction.build()).build();
    }

    /**
     * Method converts OF SetMplsTTL action object to SAL SetMplsTTL action
     * object.
     *
     * @param action
     * @return
     */
    public static SetMplsTtlActionCase ofToSALSetMplsTtl(Action action) {

        SetMplsTtlActionBuilder mplsTtlAction = new SetMplsTtlActionBuilder();
        MplsTtlAction mplsTtl = action.getAugmentation(MplsTtlAction.class);
        mplsTtlAction.setMplsTtl(mplsTtl.getMplsTtl());
        return new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(mplsTtlAction.build()).build();
    }

    /**
     * Method converts OF Pushvlan action to SAL PushVlan action.
     *
     * @param action
     * @return PushVlanAction
     */
    public static PushVlanActionCase ofToSALPushVlanAction(Action action) {

        PushVlanActionBuilder pushVlanAction = new PushVlanActionBuilder();

        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        pushVlanAction.setEthernetType(etherType.getEthertype().getValue());

        return new PushVlanActionCaseBuilder().setPushVlanAction(pushVlanAction.build()).build();
    }

    /**
     * Method converts OF PushMpls action to SAL PushMpls action.
     *
     * @param action
     * @return PushMplsAction
     */
    public static PushMplsActionCase ofToSALPushMplsAction(Action action) {

        PushMplsActionBuilder pushMplsAction = new PushMplsActionBuilder();

        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        pushMplsAction.setEthernetType(etherType.getEthertype().getValue());

        return new PushMplsActionCaseBuilder().setPushMplsAction(pushMplsAction.build()).build();
    }

    /**
     * Method converts OF PopMpls action to SAL PopMpls action.
     *
     * @param action
     * @return PopMplsActionCase
     */
    public static PopMplsActionCase ofToSALPopMplsAction(Action action) {

        PopMplsActionBuilder popMplsAction = new PopMplsActionBuilder();

        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        popMplsAction.setEthernetType(etherType.getEthertype().getValue());

        return new PopMplsActionCaseBuilder().setPopMplsAction(popMplsAction.build()).build();
    }

    /**
     * Method converts OF SetQueue action to SAL SetQueue action.
     *
     * @param action
     * @return SetQueueAction
     */
    public static SetQueueActionCase ofToSALSetQueue(Action action) {

        SetQueueActionBuilder setQueueAction = new SetQueueActionBuilder();

        QueueIdAction queueId = action.getAugmentation(QueueIdAction.class);
        setQueueAction.setQueueId(queueId.getQueueId());

        return new SetQueueActionCaseBuilder().setSetQueueAction(setQueueAction.build()).build();
    }

    /**
     * Method converts OF SetNwTtl action to SAL SetNwTtl action.
     *
     * @param action
     * @return SetNwTtlAction
     */
    public static SetNwTtlActionCase ofToSALSetNwTtl(Action action) {

        SetNwTtlActionBuilder setNwTtl = new SetNwTtlActionBuilder();
        NwTtlAction nwTtl = action.getAugmentation(NwTtlAction.class);
        setNwTtl.setNwTtl(nwTtl.getNwTtl());

        return new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtl.build()).build();
    }

    /**
     * Method converts OF Pushvlan action to SAL PushVlan action.
     *
     * @param action
     * @return PushVlanAction
     */
    public static PushPbbActionCase ofToSALPushPbbAction(Action action) {

        PushPbbActionBuilder pushPbbAction = new PushPbbActionBuilder();

        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        pushPbbAction.setEthernetType(etherType.getEthertype().getValue());

        return new PushPbbActionCaseBuilder().setPushPbbAction(pushPbbAction.build()).build();
    }

}
