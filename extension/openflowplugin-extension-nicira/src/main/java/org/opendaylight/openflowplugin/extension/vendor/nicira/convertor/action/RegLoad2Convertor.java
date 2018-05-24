/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.util.Collections;
import javax.annotation.Nullable;
import org.opendaylight.openflowjava.nx.codec.match.NiciraMatchCodecs;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NshFlagsConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NshTtlConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc1Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc2Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc3Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.Nshc4Convertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NsiConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.NspConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load2.grouping.NxActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load2.grouping.NxActionRegLoad2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNsi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.flags.grouping.NshFlagsValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.ttl.grouping.NshTtlValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsi.grouping.NsiValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsp.grouping.NspValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshFlagsCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshTtlCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NsiCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NspCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.DstChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshFlagsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshFlagsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc1Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc1CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc2Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc2CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc3Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc3CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc4Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNshc4CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNsiCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNsiCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNspCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxNspCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.DstBuilder;

/**
 * Convert between RegLoad SAL action and RegLoad2 nicira action.
 */
public class RegLoad2Convertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
                Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(Action input,
                                                                                                      ActionPath path) {
        NxActionRegLoad2 actionRegLoad2 = ((ActionRegLoad2) input.getActionChoice()).getNxActionRegLoad2();
        MatchEntry matchEntry = actionRegLoad2.getMatchEntry().get(0);
        NxRegLoad nxRegLoad = resolveRegLoad(matchEntry);
        return RegLoadConvertor.resolveAction(nxRegLoad, path);
    }

    @Override
    public Action convert(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase) {
        Preconditions.checkArgument(actionCase instanceof NxActionRegLoadGrouping);

        NxActionRegLoadGrouping nxAction = (NxActionRegLoadGrouping) actionCase;
        MatchEntry matchEntry = resolveMatchEntry(nxAction.getNxRegLoad());
        NxActionRegLoad2 nxActionRegLoad2 = new NxActionRegLoad2Builder()
                .setMatchEntry(Collections.singletonList(matchEntry))
                .build();
        ActionRegLoad2 actionRegLoad2 = new ActionRegLoad2Builder().setNxActionRegLoad2(nxActionRegLoad2).build();
        return ActionUtil.createAction(actionRegLoad2);
    }

    private static MatchEntry resolveMatchEntry(NxRegLoad nxRegLoad) {
        Dst dst = nxRegLoad.getDst();
        BigInteger value = nxRegLoad.getValue();
        int start = dst.getStart();
        int end = dst.getEnd();
        BigInteger[] valueMask = resolveValueMask(value, start, end);
        value = valueMask[0];
        BigInteger mask = valueMask[1];
        DstChoice dstChoice = dst.getDstChoice();
        return resolveMatchEntry(dstChoice, value, mask);
    }

    private static MatchEntry resolveMatchEntry(DstChoice dstChoice, BigInteger value, BigInteger mask) {
        try {
            if (dstChoice instanceof DstNxNshFlagsCase) {
                return NshFlagsConvertor.buildMatchEntry(value.shortValueExact(), mask.shortValueExact());
            }
            if (dstChoice instanceof DstNxNspCase) {
                return NspConvertor.buildMatchEntry(value.longValueExact(), mask.longValueExact());
            }
            if (dstChoice instanceof DstNxNsiCase) {
                return NsiConvertor.buildMatchEntry(value.shortValueExact(), mask.shortValueExact());
            }
            if (dstChoice instanceof DstNxNshc1Case) {
                return Nshc1Convertor.buildMatchEntry(value.longValueExact(), mask.longValueExact());
            }
            if (dstChoice instanceof DstNxNshc2Case) {
                return Nshc2Convertor.buildMatchEntry(value.longValueExact(), mask.longValueExact());
            }
            if (dstChoice instanceof DstNxNshc3Case) {
                return Nshc3Convertor.buildMatchEntry(value.longValueExact(), mask.longValueExact());
            }
            if (dstChoice instanceof DstNxNshc4Case) {
                return Nshc4Convertor.buildMatchEntry(value.longValueExact(), mask.longValueExact());
            }
            if (dstChoice instanceof DstNxNshTtlCase) {
                return NshTtlConvertor.buildMatchEntry(value.shortValueExact(), mask.shortValueExact());
            }
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Value or bit range too big for destination choice", e);
        }

        throw new CodecPreconditionException("Missing implementation of a case in dst-choice? " + dstChoice.getClass());
    }

    private static NxRegLoad resolveRegLoad(MatchEntry matchEntry) {
        Class<? extends MatchField> oxmMatchField = matchEntry.getOxmMatchField();
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) matchEntry.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NxExpMatchEntryValue nxExpMatchEntryValue = ofjAugNxExpMatch.getNxExpMatchEntryValue();
        DstBuilder dstBuilder = new DstBuilder();
        return resolveRegLoad(oxmMatchField, nxExpMatchEntryValue, dstBuilder);
    }

    private static NxRegLoad resolveRegLoad(
            Class<? extends MatchField> oxmMatchField,
            NxExpMatchEntryValue value,
            DstBuilder dstBuilder) {

        if (NxmNxNshFlags.class.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSH_FLAGS_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshFlagsCaseBuilder().setNxNshFlags(true).build());
            NshFlagsValues nshFlagsValues = ((NshFlagsCaseValue) value).getNshFlagsValues();
            return resolveRegLoad(nshFlagsValues.getNshFlags(), nshFlagsValues.getMask(), valueLength, dstBuilder);
        }
        if (NxmNxNsp.class.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSP_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNspCaseBuilder().setNxNspDst(true).build());
            NspValues nspValues = ((NspCaseValue) value).getNspValues();
            return resolveRegLoad(nspValues.getNsp(), nspValues.getMask(), valueLength, dstBuilder);
        }
        if (NxmNxNsi.class.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSI_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNsiCaseBuilder().setNxNsiDst(true).build());
            NsiValues nsiValues = ((NsiCaseValue) value).getNsiValues();
            return resolveRegLoad(nsiValues.getNsi(), nsiValues.getMask(), valueLength, dstBuilder);
        }
        if (NxmNxNshc1.class.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSC1_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshc1CaseBuilder().setNxNshc1Dst(true).build());
            NshcCaseValue nshcCaseValue = ((NshcCaseValue) value);
            return resolveRegLoad(nshcCaseValue.getNshc(), nshcCaseValue.getMask(), valueLength, dstBuilder);
        }
        if (NxmNxNshc2.class.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSC2_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshc2CaseBuilder().setNxNshc2Dst(true).build());
            NshcCaseValue nshcCaseValue = ((NshcCaseValue) value);
            return resolveRegLoad(nshcCaseValue.getNshc(), nshcCaseValue.getMask(), valueLength, dstBuilder);
        }
        if (NxmNxNshc3.class.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSC3_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshc3CaseBuilder().setNxNshc3Dst(true).build());
            NshcCaseValue nshcCaseValue = ((NshcCaseValue) value);
            return resolveRegLoad(nshcCaseValue.getNshc(), nshcCaseValue.getMask(), valueLength, dstBuilder);
        }
        if (NxmNxNshc4.class.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSC4_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshc4CaseBuilder().setNxNshc4Dst(true).build());
            NshcCaseValue nshcCaseValue = ((NshcCaseValue) value);
            return resolveRegLoad(nshcCaseValue.getNshc(), nshcCaseValue.getMask(), valueLength, dstBuilder);
        }
        if (NxmNxNshTtl.class.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSH_TTL_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshTtlCaseBuilder().setNxNshTtl(true).build());
            NshTtlValues nshTtlValues = ((NshTtlCaseValue) value).getNshTtlValues();
            return resolveRegLoad(nshTtlValues.getNshTtl(), nshTtlValues.getMask(), valueLength, dstBuilder);
        }

        throw new CodecPreconditionException("Missing codec for " + value.getImplementedInterface());
    }

    private static NxRegLoad resolveRegLoad(Short value, @Nullable Short mask, int valueLength, DstBuilder dstBuilder) {
        return resolveRegLoad(
                BigInteger.valueOf(value),
                mask == null ? null : BigInteger.valueOf(mask),
                valueLength,
                dstBuilder);
    }

    private static NxRegLoad resolveRegLoad(Long value, @Nullable Long mask, int valueLength, DstBuilder dstBuilder) {
        return resolveRegLoad(
                BigInteger.valueOf(value),
                mask == null ? null : BigInteger.valueOf(mask),
                valueLength,
                dstBuilder);
    }

    // Convert the value/mask pair of the openflowjava reg_load2 action to the
    // value/bit range pair of the openfloplugin reg_load action.
    private static NxRegLoad resolveRegLoad(BigInteger value,
                                            @Nullable BigInteger mask,
                                            int length,
                                            DstBuilder dstBuilder) {
        final int start;
        final int end;
        if (mask == null) {
            start = 0;
            end = length * 8;
        } else {
            start = mask.getLowestSetBit();
            end = start + mask.shiftRight(start).not().getLowestSetBit();
            value = value.and(mask).shiftRight(start);

            if (value.bitLength() > end - start) {
                // We cannot map a REG_LOAD2 to a single REG_LOAD if the mask
                // has multiple 1-bit segments (i.e. 0xFF00FF)
                throw new IllegalArgumentException("Value does not fit in the first 1-bit segment of the mask");
            }
        }

        dstBuilder.setStart(start);
        dstBuilder.setEnd(end - 1);
        NxRegLoadBuilder nxRegLoadBuilder = new NxRegLoadBuilder();
        nxRegLoadBuilder.setDst(dstBuilder.build());
        nxRegLoadBuilder.setValue(value);
        return nxRegLoadBuilder.build();
    }

    // Convert value/bit range pair of the openfloplugin reg_load action to the
    // value/mask pair of the openflowjava reg_load2 action.
    private static BigInteger[] resolveValueMask(BigInteger value, int start, int end) {
        int bits = end - start + 1;
        if (value.bitLength() > bits) {
            throw new IllegalArgumentException("Value does not fit the bit range");
        }

        BigInteger mask = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE).shiftLeft(start);
        value = value.shiftLeft(start);

        return new BigInteger[] {value, mask};
    }

}
