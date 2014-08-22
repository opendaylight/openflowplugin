/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.err.*;

import static junit.framework.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.err.ErrorType.*;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Additional unit tests for OfmError messages.
 *
 * @author Simon Hunt
 */
public class OfmErrorTestAdditional extends OfmTest {

    private static class ErrorCombo {
        private final ErrorType type;
        private final ErrorCode code;

        private ErrorCombo(ErrorType type, ErrorCode code) {
            this.type = type;
            this.code = code;
        }

        private void verify(ProtocolVersion pv, boolean allowed) {
            try {
                OfmMutableError err = (OfmMutableError)
                        create(pv, MessageType.ERROR, type);
                err.errorCode(code);
                OfmError msg = (OfmError) err.toImmutable();
                print(msg);
                if (!allowed)
                    fail(AM_NOEX);

            } catch (VersionMismatchException vme) {
                if (!allowed) {
                    print(FMT_EX, vme);
                } else {
                    print(vme);
                    fail(AM_UNEX);
                }

            } catch (Exception e) {
                fail(AM_UNEX);
            }
        }
    }

    private ErrorCombo combo;


    @Test
    public void helloFailedIncompatible() {
        print(EOL + "helloFailedIncompatible()");
        combo = new ErrorCombo(HELLO_FAILED, ECodeHelloFailed.INCOMPATIBLE);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void helloFailedEperm() {
        print(EOL + "helloFailedEperm()");
        combo = new ErrorCombo(HELLO_FAILED, ECodeHelloFailed.EPERM);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBadVersion() {
        print(EOL + "badRequestBadVersion()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BAD_VERSION);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBadType() {
        print(EOL + "badRequestBadType()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BAD_TYPE);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBadStat() {
        print(EOL + "badRequestBadStat()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BAD_STAT);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBadExperimenter() {
        print(EOL + "badRequestBadExperimenter()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BAD_EXPERIMENTER);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBadExpType() {
        print(EOL + "badRequestBadExpType()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BAD_EXP_TYPE);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestEperm() {
        print(EOL + "badRequestEperm()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.EPERM);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBadLen() {
        print(EOL + "badRequestBadLen()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BAD_LEN);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBufferEmpty() {
        print(EOL + "badRequestBufferEmpty()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BUFFER_EMPTY);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBufferUnknown() {
        print(EOL + "badRequestBufferUnknown()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BUFFER_UNKNOWN);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBadTableId() {
        print(EOL + "badRequestBadTableId()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BAD_TABLE_ID);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestIsSlave() {
        print(EOL + "badRequestIsSlave()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.IS_SLAVE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBadPort() {
        print(EOL + "badRequestBadPort()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BAD_PORT);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestBadPacket() {
        print(EOL + "badRequestBadPacket()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.BAD_PACKET);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badRequestMpBufferOverflow() {
        print(EOL + "badRequestMpBufferOverflow()");
        combo = new ErrorCombo(BAD_REQUEST, ECodeBadRequest.MP_BUFFER_OVERFLOW);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadType() {
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_TYPE);
        print(EOL + "badActionBadType()");
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadLen() {
        print(EOL + "badActionBadLen()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_LEN);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadExperimenter() {
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_EXPERIMENTER);
        print(EOL + "badActionBadExperimenter()");
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadExpType() {
        print(EOL + "badActionBadExpType()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_EXP_TYPE);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadOutPort() {
        print(EOL + "badActionBadOutPort()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_OUT_PORT);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadArgument() {
        print(EOL + "badActionBadArgument()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_ARGUMENT);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionEperm() {
        print(EOL + "badActionBadEperm()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.EPERM);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionTooMany() {
        print(EOL + "badActionTooMany()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.TOO_MANY);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadQueue() {
        print(EOL + "badActionBadQueue()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_QUEUE);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadOutGroup() {
        print(EOL + "badActionBadOutGroup()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_OUT_GROUP);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionMatchInconsistent() {
        print(EOL + "badActionMatchInconsistent()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.MATCH_INCONSISTENT);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionUnsupportedOrder() {
        print(EOL + "badActionUnsupportedOrder()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.UNSUPPORTED_ORDER);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadTag() {
        print(EOL + "badActionBadTag()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_TAG);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadSetType() {
        print(EOL + "badActionBadSetType()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_SET_TYPE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadSetLen() {
        print(EOL + "badActionBadSetLen()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_SET_LEN);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badActionBadSetArgument() {
        print(EOL + "badActionBadSetArgument()");
        combo = new ErrorCombo(BAD_ACTION, ECodeBadAction.BAD_SET_ARGUMENT);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badInstructionUnknownInst() {
        print(EOL + "badInstructionUnknownInst()");
        combo = new ErrorCombo(BAD_INSTRUCTION, ECodeBadInstruction.UNKNOWN_INST);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badInstructionUnsupInst() {
        print(EOL + "badInstructionUnsupInst()");
        combo = new ErrorCombo(BAD_INSTRUCTION, ECodeBadInstruction.UNSUP_INST);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badInstructionBadTableId() {
        print(EOL + "badInstructionBadTableId()");
        combo = new ErrorCombo(BAD_INSTRUCTION, ECodeBadInstruction.BAD_TABLE_ID);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badInstructionUnsupMetadata() {
        print(EOL + "badInstructionUnsupMetadata()");
        combo = new ErrorCombo(BAD_INSTRUCTION, ECodeBadInstruction.UNSUP_METADATA);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badInstructionUnsupMetadataMask() {
        print(EOL + "badInstructionUnsupMetadataMask()");
        combo = new ErrorCombo(BAD_INSTRUCTION, ECodeBadInstruction.UNSUP_METADATA_MASK);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badInstructionBadExperimenter() {
        print(EOL + "badInstructionBadExperimenter()");
        combo = new ErrorCombo(BAD_INSTRUCTION, ECodeBadInstruction.BAD_EXPERIMENTER);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badInstructionBadExpType() {
        print(EOL + "badInstructionBadExpType()");
        combo = new ErrorCombo(BAD_INSTRUCTION, ECodeBadInstruction.BAD_EXP_TYPE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badInstructionBadLen() {
        print(EOL + "badInstructionBadLen()");
        combo = new ErrorCombo(BAD_INSTRUCTION, ECodeBadInstruction.BAD_LEN);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badInstructionEperm() {
        print(EOL + "badInstructionEperm()");
        combo = new ErrorCombo(BAD_INSTRUCTION, ECodeBadInstruction.EPERM);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchBadType() {
        print(EOL + "badMatchBadType()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.BAD_TYPE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchBadLen() {
        print(EOL + "badMatchBadLen()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.BAD_LEN);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchBadTag() {
        print(EOL + "badMatchBadTag()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.BAD_TAG);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchBadDlAddrMask() {
        print(EOL + "badMatchBadDlAddrMask()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.BAD_DL_ADDR_MASK);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchBadNwAddrMask() {
        print(EOL + "badMatchBadNwAddrMask()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.BAD_NW_ADDR_MASK);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchBadWildcards() {
        print(EOL + "badMatchBadWildcards()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.BAD_WILDCARDS);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchBadField() {
        print(EOL + "badMatchBadField()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.BAD_FIELD);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchBadValue() {
        print(EOL + "badMatchBadValue()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.BAD_VALUE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchBadMask() {
        print(EOL + "badMatchBadMask()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.BAD_MASK);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchBadPrereq() {
        print(EOL + "badMatchBadPrereq()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.BAD_PREREQ);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchDupField() {
        print(EOL + "badMatchDupField()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.DUP_FIELD);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void badMatchEperm() {
        print(EOL + "badMatchEperm()");
        combo = new ErrorCombo(BAD_MATCH, ECodeBadMatch.EPERM);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void flowModFailedUnknown() {
        print(EOL + "flowModFailedUnknown()");
        combo = new ErrorCombo(FLOW_MOD_FAILED, ECodeFlowModFailed.UNKNOWN);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void flowModFailedTableFull() {
        print(EOL + "flowModFailedTableFull()");
        combo = new ErrorCombo(FLOW_MOD_FAILED, ECodeFlowModFailed.TABLE_FULL);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void flowModFailedBadTableId() {
        print(EOL + "flowModFailedBadTableId()");
        combo = new ErrorCombo(FLOW_MOD_FAILED, ECodeFlowModFailed.BAD_TABLE_ID);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void flowModFailedOverlap() {
        print(EOL + "flowModFailedOverlap()");
        combo = new ErrorCombo(FLOW_MOD_FAILED, ECodeFlowModFailed.OVERLAP);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void flowModFailedEperm() {
        print(EOL + "flowModFailedEperm()");
        combo = new ErrorCombo(FLOW_MOD_FAILED, ECodeFlowModFailed.EPERM);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void flowModFailedBadTimeout() {
        print(EOL + "flowModFailedBadTimeout()");
        combo = new ErrorCombo(FLOW_MOD_FAILED, ECodeFlowModFailed.BAD_TIMEOUT);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void flowModFailedBadCommand() {
        print(EOL + "flowModFailedBadCommand()");
        combo = new ErrorCombo(FLOW_MOD_FAILED, ECodeFlowModFailed.BAD_COMMAND);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void flowModFailedUnsupportedActionList() {
        print(EOL + "flowModFailedUnsupportedActionList()");
        combo = new ErrorCombo(FLOW_MOD_FAILED, ECodeFlowModFailed.UNSUPPORTED_ACTION_LIST);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, false);
    }

    @Test
    public void flowModFailedBadFlags() {
        print(EOL + "flowModFailedBadFlags()");
        combo = new ErrorCombo(FLOW_MOD_FAILED, ECodeFlowModFailed.BAD_FLAGS);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedGroupExists() {
        print(EOL + "groupModFailedGroupExists()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.GROUP_EXISTS);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedInvalidGroup() {
        print(EOL + "groupModFailedInvalidGroup()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.INVALID_GROUP);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedWeightUnsupported() {
        print(EOL + "groupModFailedWeightUnsupported()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.WEIGHT_UNSUPPORTED);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedOutOfGroups() {
        print(EOL + "groupModFailedOutOfGroups()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.OUT_OF_GROUPS);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedOutOfBuckets() {
        print(EOL + "groupModFailedOutOfBuckets()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.OUT_OF_BUCKETS);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedChainingUnsupported() {
        print(EOL + "groupModFailedChainingUnsupported()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.CHAINING_UNSUPPORTED);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedWatchUnsupported() {
        print(EOL + "groupModFailedWatchUnsupported()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.WATCH_UNSUPPORTED);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedLoop() {
        print(EOL + "groupModFailedLoop()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.LOOP);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedUnknownGroup() {
        print(EOL + "groupModFailedUnknownGroup()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.UNKNOWN_GROUP);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedChainedGroup() {
        print(EOL + "groupModFailedChainedGroup()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.CHAINED_GROUP);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedBadType() {
        print(EOL + "groupModFailedBadType()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.BAD_TYPE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedBadCommand() {
        print(EOL + "groupModFailedBadCommand()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.BAD_COMMAND);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedBadBucket() {
        print(EOL + "groupModFailedBadBucket()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.BAD_BUCKET);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedBadWatch() {
        print(EOL + "groupModFailedBadWatch()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.BAD_WATCH);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void groupModFailedEperm() {
        print(EOL + "groupModFailedEperm()");
        combo = new ErrorCombo(GROUP_MOD_FAILED, ECodeGroupModFailed.EPERM);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void portModFailedBadPort() {
        print(EOL + "portModFailedBadPort()");
        combo = new ErrorCombo(PORT_MOD_FAILED, ECodePortModFailed.BAD_PORT);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void portModFailedBadHwAddr() {
        print(EOL + "portModFailedBadHwAddr()");
        combo = new ErrorCombo(PORT_MOD_FAILED, ECodePortModFailed.BAD_HW_ADDR);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void portModFailedBadConfig() {
        print(EOL + "portModFailedBadConfig()");
        combo = new ErrorCombo(PORT_MOD_FAILED, ECodePortModFailed.BAD_CONFIG);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void portModFailedBadAdvertise() {
        print(EOL + "portModFailedBadAdvertise()");
        combo = new ErrorCombo(PORT_MOD_FAILED, ECodePortModFailed.BAD_ADVERTISE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void portModFailedEperm() {
        print(EOL + "portModFailedEperm()");
        combo = new ErrorCombo(PORT_MOD_FAILED, ECodePortModFailed.EPERM);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void tableModFailedBadTable() {
        print(EOL + "tableModFailedBadTable()");
        combo = new ErrorCombo(TABLE_MOD_FAILED, ECodeTableModFailed.BAD_TABLE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void tableModFailedBadConfig() {
        print(EOL + "tableModFailedBadConfig()");
        combo = new ErrorCombo(TABLE_MOD_FAILED, ECodeTableModFailed.BAD_CONFIG);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void tableModFailedEperm() {
        print(EOL + "tableModFailedEperm()");
        combo = new ErrorCombo(TABLE_MOD_FAILED, ECodeTableModFailed.EPERM);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void queueOpFailedBadPort() {
        print(EOL + "queueOpFailedBadPort()");
        combo = new ErrorCombo(QUEUE_OP_FAILED, ECodeQueueOpFailed.BAD_PORT);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void queueOpFailedBadQueue() {
        print(EOL + "queueOpFailedBadQueue()");
        combo = new ErrorCombo(QUEUE_OP_FAILED, ECodeQueueOpFailed.BAD_QUEUE);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void queueOpFailedEperm() {
        print(EOL + "queueOpFailedEperm()");
        combo = new ErrorCombo(QUEUE_OP_FAILED, ECodeQueueOpFailed.EPERM);
        combo.verify(V_1_0, true);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void switchConfigFailedBadFlags() {
        print(EOL + "switchConfigFailedBadFlags()");
        combo = new ErrorCombo(SWITCH_CONFIG_FAILED, ECodeSwitchConfigFailed.BAD_FLAGS);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void switchConfigFailedBadLen() {
        print(EOL + "switchConfigFailedBadLen()");
        combo = new ErrorCombo(SWITCH_CONFIG_FAILED, ECodeSwitchConfigFailed.BAD_LEN);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, true);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void switchConfigFailedEperm() {
        print(EOL + "switchConfigFailedEperm()");
        combo = new ErrorCombo(SWITCH_CONFIG_FAILED, ECodeSwitchConfigFailed.EPERM);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void roleRequestFailedStale() {
        print(EOL + "roleRequestFailedStale()");
        combo = new ErrorCombo(ROLE_REQUEST_FAILED, ECodeRoleRequestFailed.STALE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void roleRequestFailedUnsup() {
        print(EOL + "roleRequestFailedUnsup()");
        combo = new ErrorCombo(ROLE_REQUEST_FAILED, ECodeRoleRequestFailed.UNSUP);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void roleRequestFailedBadRole() {
        print(EOL + "roleRequestFailedBadRole()");
        combo = new ErrorCombo(ROLE_REQUEST_FAILED, ECodeRoleRequestFailed.BAD_ROLE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, true);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedUnknown() {
        print(EOL + "meterModFailedUnknown()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.UNKNOWN);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedMeterExists() {
        print(EOL + "meterModFailedMeterExists()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.METER_EXISTS);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedInvalidMeter() {
        print(EOL + "meterModFailedInvalidMeter()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.INVALID_METER);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedUnknownMeter() {
        print(EOL + "meterModFailedUnknownMeter()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.UNKNOWN_METER);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedBadCommand() {
        print(EOL + "meterModFailedBadCommand()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.BAD_COMMAND);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedBadFlags() {
        print(EOL + "meterModFailedBadFlags()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.BAD_FLAGS);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedBadRate() {
        print(EOL + "meterModFailedBadRate()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.BAD_RATE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedBadBurst() {
        print(EOL + "meterModFailedBadBurst()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.BAD_BURST);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedBadBand() {
        print(EOL + "meterModFailedBadBand()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.BAD_BAND);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedBadBandValue() {
        print(EOL + "meterModFailedBadBandValue()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.BAD_BAND_VALUE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedOutOfMeters() {
        print(EOL + "meterModFailedOutOfMeters()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.OUT_OF_METERS);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void meterModFailedOutOfBands() {
        print(EOL + "meterModFailedOutOfBands()");
        combo = new ErrorCombo(METER_MOD_FAILED, ECodeMeterModFailed.OUT_OF_BANDS);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void tableFeaturesFailedBadTable() {
        print(EOL + "tableFeaturesFailedBadTable()");
        combo = new ErrorCombo(TABLE_FEATURES_FAILED, ECodeTableFeaturesFailed.BAD_TABLE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void tableFeaturesFailedBadMetadata() {
        print(EOL + "tableFeaturesFailedBadMetadata()");
        combo = new ErrorCombo(TABLE_FEATURES_FAILED, ECodeTableFeaturesFailed.BAD_METADATA);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void tableFeaturesFailedBadType() {
        print(EOL + "tableFeaturesFailedBadType()");
        combo = new ErrorCombo(TABLE_FEATURES_FAILED, ECodeTableFeaturesFailed.BAD_TYPE);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void tableFeaturesFailedBadLen() {
        print(EOL + "tableFeaturesFailedBadLen()");
        combo = new ErrorCombo(TABLE_FEATURES_FAILED, ECodeTableFeaturesFailed.BAD_LEN);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void tableFeaturesFailedBadArgument() {
        print(EOL + "tableFeaturesFailedBadArgument()");
        combo = new ErrorCombo(TABLE_FEATURES_FAILED, ECodeTableFeaturesFailed.BAD_ARGUMENT);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

    @Test
    public void tableFeaturesFailedEperm() {
        print(EOL + "tableFeaturesFailedEperm()");
        combo = new ErrorCombo(TABLE_FEATURES_FAILED, ECodeTableFeaturesFailed.EPERM);
        combo.verify(V_1_0, false);
        combo.verify(V_1_1, false);
        combo.verify(V_1_2, false);
        combo.verify(V_1_3, true);
    }

}
