/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.Collections;
import org.eclipse.jdt.annotation.Nullable;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.match.entry.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionRegLoad2Builder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshFlagsCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshTtlCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NsiCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NspCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.flags.grouping.NshFlagsValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.ttl.grouping.NshTtlValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsi.grouping.NsiValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsp.grouping.NspValues;
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
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Convert between RegLoad SAL action and RegLoad2 nicira action.
 */
public class RegLoad2Convertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
                Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(
            final Action input, final ActionPath path) {
        NxActionRegLoad2 actionRegLoad2 = ((ActionRegLoad2) input.getActionChoice()).getNxActionRegLoad2();
        MatchEntry matchEntry = actionRegLoad2.getMatchEntry().get(0);
        NxRegLoad nxRegLoad = resolveRegLoad(matchEntry);
        return RegLoadConvertor.resolveAction(nxRegLoad, path);
    }

    @Override
    public Action convert(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase) {
        Preconditions.checkArgument(actionCase instanceof NxActionRegLoadGrouping);

        NxActionRegLoadGrouping nxAction = (NxActionRegLoadGrouping) actionCase;
        MatchEntry matchEntry = resolveMatchEntry(nxAction.getNxRegLoad());
        NxActionRegLoad2 nxActionRegLoad2 = new NxActionRegLoad2Builder()
                .setMatchEntry(Collections.singletonList(matchEntry))
                .build();
        ActionRegLoad2 actionRegLoad2 = new ActionRegLoad2Builder().setNxActionRegLoad2(nxActionRegLoad2).build();
        return ActionUtil.createAction(actionRegLoad2);
    }

    private static MatchEntry resolveMatchEntry(final NxRegLoad nxRegLoad) {
        Dst dst = nxRegLoad.getDst();
        Uint64 value = nxRegLoad.getValue();
        Uint16 start = dst.getStart();
        Uint16 end = dst.getEnd();
        Uint64[] valueMask = resolveValueMask(value, start, end);
        value = valueMask[0];
        Uint64 mask = valueMask[1];
        DstChoice dstChoice = dst.getDstChoice();
        return resolveMatchEntry(dstChoice, value, mask);
    }

    private static MatchEntry resolveMatchEntry(final DstChoice dstChoice, final Uint64 value, final Uint64 mask) {
        try {
            if (dstChoice instanceof DstNxNshFlagsCase) {
                return NshFlagsConvertor.buildMatchEntry(Uint8.valueOf(value), Uint8.valueOf(mask));
            }
            if (dstChoice instanceof DstNxNspCase) {
                return NspConvertor.buildMatchEntry(Uint32.valueOf(value), Uint32.valueOf(mask));
            }
            if (dstChoice instanceof DstNxNsiCase) {
                return NsiConvertor.buildMatchEntry(Uint8.valueOf(value), Uint8.valueOf(mask));
            }
            if (dstChoice instanceof DstNxNshc1Case) {
                return Nshc1Convertor.buildMatchEntry(Uint32.valueOf(value), Uint32.valueOf(mask));
            }
            if (dstChoice instanceof DstNxNshc2Case) {
                return Nshc2Convertor.buildMatchEntry(Uint32.valueOf(value), Uint32.valueOf(mask));
            }
            if (dstChoice instanceof DstNxNshc3Case) {
                return Nshc3Convertor.buildMatchEntry(Uint32.valueOf(value), Uint32.valueOf(mask));
            }
            if (dstChoice instanceof DstNxNshc4Case) {
                return Nshc4Convertor.buildMatchEntry(Uint32.valueOf(value), Uint32.valueOf(mask));
            }
            if (dstChoice instanceof DstNxNshTtlCase) {
                return NshTtlConvertor.buildMatchEntry(Uint8.valueOf(value), Uint8.valueOf(mask));
            }
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Value or bit range too big for destination choice", e);
        }

        throw new CodecPreconditionException("Missing implementation of a case in dst-choice? " + dstChoice.getClass());
    }

    private static NxRegLoad resolveRegLoad(final MatchEntry matchEntry) {
        MatchField oxmMatchField = matchEntry.getOxmMatchField();
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) matchEntry.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NxExpMatchEntryValue nxExpMatchEntryValue = ofjAugNxExpMatch.getNxExpMatchEntryValue();
        DstBuilder dstBuilder = new DstBuilder();
        return resolveRegLoad(oxmMatchField, nxExpMatchEntryValue, dstBuilder);
    }

    private static NxRegLoad resolveRegLoad(
            final MatchField oxmMatchField,
            final NxExpMatchEntryValue value,
            final DstBuilder dstBuilder) {

        if (NxmNxNshFlags.VALUE.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSH_FLAGS_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshFlagsCaseBuilder().setNxNshFlags(Empty.value()).build());
            NshFlagsValues nshFlagsValues = ((NshFlagsCaseValue) value).getNshFlagsValues();
            return resolveRegLoad(nshFlagsValues.getNshFlags(), nshFlagsValues.getMask(), valueLength, dstBuilder);
        } else if (NxmNxNsp.VALUE.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSP_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNspCaseBuilder().setNxNspDst(Empty.value()).build());
            NspValues nspValues = ((NspCaseValue) value).getNspValues();
            return resolveRegLoad(nspValues.getNsp(), nspValues.getMask(), valueLength, dstBuilder);
        } else if (NxmNxNsi.VALUE.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSI_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNsiCaseBuilder().setNxNsiDst(Empty.value()).build());
            NsiValues nsiValues = ((NsiCaseValue) value).getNsiValues();
            return resolveRegLoad(nsiValues.getNsi(), nsiValues.getMask(), valueLength, dstBuilder);
        } else if (NxmNxNshc1.VALUE.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSC1_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshc1CaseBuilder().setNxNshc1Dst(Empty.value()).build());
            NshcCaseValue nshcCaseValue = (NshcCaseValue) value;
            return resolveRegLoad(nshcCaseValue.getNshc(), nshcCaseValue.getMask(), valueLength, dstBuilder);
        } else if (NxmNxNshc2.VALUE.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSC2_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshc2CaseBuilder().setNxNshc2Dst(Empty.value()).build());
            NshcCaseValue nshcCaseValue = (NshcCaseValue) value;
            return resolveRegLoad(nshcCaseValue.getNshc(), nshcCaseValue.getMask(), valueLength, dstBuilder);
        } else if (NxmNxNshc3.VALUE.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSC3_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshc3CaseBuilder().setNxNshc3Dst(Empty.value()).build());
            NshcCaseValue nshcCaseValue = (NshcCaseValue) value;
            return resolveRegLoad(nshcCaseValue.getNshc(), nshcCaseValue.getMask(), valueLength, dstBuilder);
        } else if (NxmNxNshc4.VALUE.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSC4_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshc4CaseBuilder().setNxNshc4Dst(Empty.value()).build());
            NshcCaseValue nshcCaseValue = (NshcCaseValue) value;
            return resolveRegLoad(nshcCaseValue.getNshc(), nshcCaseValue.getMask(), valueLength, dstBuilder);
        } else if (NxmNxNshTtl.VALUE.equals(oxmMatchField)) {
            int valueLength = NiciraMatchCodecs.NSH_TTL_CODEC.getValueLength();
            dstBuilder.setDstChoice(new DstNxNshTtlCaseBuilder().setNxNshTtl(Empty.value()).build());
            NshTtlValues nshTtlValues = ((NshTtlCaseValue) value).getNshTtlValues();
            return resolveRegLoad(nshTtlValues.getNshTtl(), nshTtlValues.getMask(), valueLength, dstBuilder);
        }

        throw new CodecPreconditionException("Missing codec for " + value.implementedInterface());
    }

    private static NxRegLoad resolveRegLoad(final Uint8 value, final @Nullable Uint8 mask, final int valueLength,
            final DstBuilder dstBuilder) {
        return resolveRegLoad(value.toUint64(), mask == null ? null : mask.toUint64(), valueLength, dstBuilder);
    }

    private static NxRegLoad resolveRegLoad(final Uint32 value, final @Nullable Uint32 mask, final int valueLength,
            final DstBuilder dstBuilder) {
        return resolveRegLoad(value.toUint64(), mask == null ? null : mask.toUint64(), valueLength, dstBuilder);
    }

    // Convert the value/mask pair of the openflowjava reg_load2 action to the
    // value/bit range pair of the openfloplugin reg_load action.
    private static NxRegLoad resolveRegLoad(Uint64 value, final @Nullable Uint64 mask, final int length,
            final DstBuilder dstBuilder) {
        final int start;
        final int end;
        if (mask == null) {
            start = 0;
            end = length * 8;
        } else {
            final long maskBits = mask.longValue();
            start = lowestSetBit(maskBits);
            end = start + lowestSetBit(~(maskBits >> start));

            final long valueBits = value.longValue();
            final long newValueBits = (valueBits & maskBits) >> start;
            final int bitLength = bitLength(newValueBits);
            if (bitLength > end - start) {
                // We cannot map a REG_LOAD2 to a single REG_LOAD if the mask
                // has multiple 1-bit segments (i.e. 0xFF00FF)
                throw new IllegalArgumentException("Value does not fit in the first 1-bit segment of the mask");
            }

            value = Uint64.fromLongBits(newValueBits);
        }

        return new NxRegLoadBuilder()
            .setDst(dstBuilder.setStart(Uint16.valueOf(start)).setEnd(Uint16.valueOf(end - 1)).build())
            .setValue(value)
            .build();
    }

    private static int lowestSetBit(final long value) {
        return LongMath.log2(Long.lowestOneBit(value), RoundingMode.UNNECESSARY);
    }

    private static int bitLength(final long value) {
        return 64 - Long.numberOfLeadingZeros(value);
    }

    // Convert value/bit range pair of the openfloplugin reg_load action to the
    // value/mask pair of the openflowjava reg_load2 action.
    private static Uint64[] resolveValueMask(final Uint64 value, final Uint16 start, final Uint16 end) {
        final int startInt = start.toJava();
        final int bits = end.toJava() - startInt + 1;
        final long valueBits = value.longValue();
        if (bitLength(valueBits) > bits) {
            throw new IllegalArgumentException("Value does not fit the bit range");
        }

        final long maskBits = (1L << bits) - 1 << startInt;
        return new Uint64[] {Uint64.fromLongBits(valueBits << startInt), Uint64.fromLongBits(maskBits)};
    }
}
