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
import org.opendaylight.of.lib.err.*;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.ERROR;
import static org.opendaylight.util.StringUtils.toCamelCase;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the {@link OfmError} message.
 *
 * @author Simon Hunt
 * @author Pramod Shanbhag
 */
public class OfmErrorTest extends OfmTest {

    private static final String PVDIR[] = {"v10", "v11", "v12", "v13" };

    private static final String EXP_HELLO_INC_MSG = "Not Compatible, Sorry!";
    private static final byte[] FAKE_DATA = new byte[64];

    static {
        for (int i=0; i<FAKE_DATA.length; i++)
            FAKE_DATA[i] = 1;
            FAKE_DATA[62] = 0x77;
    }

    private MutableMessage mm;

    /* IMPLEMENTATION NOTE:
     *   The library supports parsing, creating, and encoding of ERROR messages
     *   in ALL the protocol versions. However, it does throw version mismatch
     *   exception if the error type/code combination was not defined for a
     *   specific version.
     */

    /** Derives a test file path from the given parameters.
     *
     * @param pv the protocol version
     * @param type the error type
     * @param code the error code
     * @return the derived file path
     */
    private static String derivedFile(ProtocolVersion pv, ErrorType type,
            ErrorCode code) {
        return derivedFile(pv, type, code, "");
    }

    /** Derives a test file path from the given parameters.
     *
     * @param pv the protocol version
     * @param type the error type
     * @param code the error code
     * @param suffix a suffix
     * @return the derived file path
     */
    private static String derivedFile(ProtocolVersion pv, ErrorType type,
            ErrorCode code, String suffix) {
        return PVDIR[pv.ordinal()] + "/err/" +
                toCamelCase(toCamelCase(type), code.toString()) + suffix;
    }


    // ================================================= SUPPORT METHODS ====

    private void checkRounding(String s, int expSize) {
        assertEquals(AM_UXS, expSize, OfmMutableError.roundedUpArrayLength(s));
    }

    @Test
    public void roundedUpArray() {
        print(EOL + "roundedUpArray()");
        // header,type,code,data
        checkRounding("", 4);           // [  8  ][ 2 ][ 2 ][....] = 16
        checkRounding("A", 4);          // [  8  ][ 2 ][ 2 ][A...] = 16
        checkRounding("AB", 4);         // [  8  ][ 2 ][ 2 ][AB..] = 16
        checkRounding("ABC", 4);        // [  8  ][ 2 ][ 2 ][ABC.] = 16
        checkRounding("ABCD", 4);       // [  8  ][ 2 ][ 2 ][ABCD] = 16
        checkRounding("ABCDE", 12);     // [  8  ][ 2 ][ 2 ][ABCDE.......] = 24
        checkRounding("ABCDEF", 12);    // [  8  ][ 2 ][ 2 ][ABCDEF......] = 24
        checkRounding("ABCDEFG", 12);   // [  8  ][ 2 ][ 2 ][ABCDEFG.....] = 24
    }


    // ========================================================= PARSING ====

    private void verifyErrorParams(OfmError msg,
            ErrorType expType, ErrorCode expCode,
            int expDataLen) {
        assertEquals(AM_NEQ, expType, msg.getErrorType());
        assertEquals(AM_NEQ, expCode, msg.getErrorCode());
        assertEquals(AM_UXS, expDataLen, msg.getData().length);
    }

    // ====

    private void verifyHelloFailedInc(ProtocolVersion pv) {
        String file = derivedFile(pv, ErrorType.HELLO_FAILED,
                ECodeHelloFailed.INCOMPATIBLE);
        OfmError msg = (OfmError) verifyMsgHeader(file, pv, ERROR, 40);
        verifyErrorParams(msg, ErrorType.HELLO_FAILED,
                ECodeHelloFailed.INCOMPATIBLE, 28);
        assertEquals(AM_NEQ, EXP_HELLO_INC_MSG, msg.getErrorMessage());
    }

    @Test
    public void errorHelloInc13() {
        print(EOL + "errorHelloInc13()");
        verifyHelloFailedInc(V_1_3);
    }

    @Test
    public void errorHelloInc12() {
        print(EOL + "errorHelloInc12()");
        verifyHelloFailedInc(V_1_2);
    }

    @Test
    public void errorHelloInc11() {
        print(EOL + "errorHelloInc11()");
        verifyHelloFailedInc(V_1_1);
    }

    @Test
    public void errorHelloInc10() {
        print(EOL + "errorHelloInc10()");
        verifyHelloFailedInc(V_1_0);
    }

    // =========================================================
    private void verifyErrorMessage(ProtocolVersion pv,
            ErrorType type, ErrorCode code) {
        String file = derivedFile(pv, type, code);
        OfmError msg = (OfmError) verifyMsgHeader(file, pv, ERROR, 76);
        verifyErrorParams(msg, type, code, 64);
        byte[] data = msg.getData();
        for (int i=0; i<data.length; i++)
            assertEquals(AM_NEQ, i==62 ? 0x77 : 0x01, data[i]);
    }

    // =========================================================
    private void verifyBadReqBuffEmpty(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.BAD_REQUEST,
                ECodeBadRequest.BUFFER_EMPTY);
    }

    @Test
    public void errorBadReqBuffEmpty13() {
        print(EOL + "errorBadReqBuffEmpty13()");
        verifyBadReqBuffEmpty(V_1_3);
    }

    @Test
    public void errorBadReqBuffEmpty12() {
        print(EOL + "errorBadReqBuffEmpty12()");
        verifyBadReqBuffEmpty(V_1_2);
    }

    @Test
    public void errorBadReqBuffEmpty11() {
        print(EOL + "errorBadReqBuffEmpty11()");
        verifyBadReqBuffEmpty(V_1_1);
    }

    @Test
    public void errorBadReqBuffEmpty10() {
        print(EOL + "errorBadReqBuffEmpty10()");
        verifyBadReqBuffEmpty(V_1_0);
    }

    // =========================================================
    private void verifyBadActionBadQueue(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.BAD_ACTION,
                ECodeBadAction.BAD_QUEUE);
    }

    @Test
    public void errorBadActionBadQueue10() {
        print(EOL + "errorBadActionBadQueue10()");
        verifyBadActionBadQueue(V_1_0);

    }

    @Test
    public void errorBadActionBadQueue11() {
        print(EOL + "errorBadActionBadQueue11()");
        verifyBadActionBadQueue(V_1_1);

    }

    @Test
    public void errorBadActionBadQueue12() {
        print(EOL + "errorBadActionBadQueue12()");
        verifyBadActionBadQueue(V_1_2);

    }

    @Test
    public void errorBadActionBadQueue13() {
        print(EOL + "errorBadActionBadQueue13()");
        verifyBadActionBadQueue(V_1_3);

    }

    // =========================================================
    private void verifyBadInstructionUnsuppMetadata(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.BAD_INSTRUCTION,
                ECodeBadInstruction.UNSUP_METADATA);
    }

    @Test
    public void errorBadInstructionUnsuppMetadata11() {
        print(EOL + "errorBadInstructionUnsuppMetadata11()");
        verifyBadInstructionUnsuppMetadata(V_1_1);
    }

    @Test
    public void errorBadInstructionUnsuppMetadata12() {
        print(EOL + "errorBadInstructionUnsuppMetadata12()");
        verifyBadInstructionUnsuppMetadata(V_1_2);
    }

    @Test
    public void errorBadInstructionUnsuppMetadata13() {
        print(EOL + "errorBadInstructionUnsuppMetadata13()");
        verifyBadInstructionUnsuppMetadata(V_1_3);
    }

    // =========================================================

    private void verifyBadMatchBadValue(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.BAD_MATCH,
                ECodeBadMatch.BAD_VALUE);
    }

    @Test
    public void errorBadMatchBadValue11() {
        print(EOL + "errorBadMatchBadValue11()");
        verifyBadMatchBadValue(V_1_1);
    }

    @Test
    public void errorBadMatchBadValue12() {
        print(EOL + "errorBadMatchBadValue12()");
        verifyBadMatchBadValue(V_1_2);
    }

    @Test
    public void errorBadMatchBadValue13() {
        print(EOL + "errorBadMatchBadValue13()");
        verifyBadMatchBadValue(V_1_3);
    }

    // =========================================================
    private void verifyFlowModFailedOverlap(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.FLOW_MOD_FAILED,
                ECodeFlowModFailed.OVERLAP);
    }

    @Test
    public void errorFlowModFailedOverlap10() {
        print(EOL + "errorFlowModFailedOverlap10()");
        verifyFlowModFailedOverlap(V_1_0);
    }

    @Test
    public void errorFlowModFailedOverlap11() {
        print(EOL + "errorFlowModFailedOverlap11()");
        verifyFlowModFailedOverlap(V_1_1);
    }

    @Test
    public void errorFlowModFailedOverlap12() {
        print(EOL + "errorFlowModFailedOverlap12()");
        verifyFlowModFailedOverlap(V_1_2);
    }

    @Test
    public void errorFlowModFailedOverlap13() {
        print(EOL + "errorFlowModFailedOverlap13()");
        verifyFlowModFailedOverlap(V_1_3);
    }

    // =========================================================
    private void verifyGroupModFailedLoop(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.GROUP_MOD_FAILED,
                ECodeGroupModFailed.LOOP);
    }

    @Test
    public void errorGroupModFailedLoop11() {
        print(EOL + "errorGroupModFailedLoop11()");
        verifyGroupModFailedLoop(V_1_1);
    }


    @Test
    public void errorGroupModFailedLoop12() {
        print(EOL + "errorGroupModFailedLoop12()");
        verifyGroupModFailedLoop(V_1_2);
    }


    @Test
    public void errorGroupModFailedLoop13() {
        print(EOL + "errorGroupModFailedLoop13()");
        verifyGroupModFailedLoop(V_1_3);
    }

    // =========================================================
    private void verifyPortModFailedBadPort(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.PORT_MOD_FAILED,
                ECodePortModFailed.BAD_PORT);
    }

    @Test
    public void errorPortModFailedBadPort10() {
        print(EOL + "errorPortModFailedBadPort10()");
        verifyPortModFailedBadPort(V_1_0);
    }

    @Test
    public void errorPortModFailedBadPort11() {
        print(EOL + "errorPortModFailedBadPort11()");
        verifyPortModFailedBadPort(V_1_1);
    }

    @Test
    public void errorPortModFailedBadPort12() {
        print(EOL + "errorPortModFailedBadPort12()");
        verifyPortModFailedBadPort(V_1_2);
    }

    @Test
    public void errorPortModFailedBadPort13() {
        print(EOL + "errorPortModFailedBadPort13()");
        verifyPortModFailedBadPort(V_1_3);
    }

    // =========================================================
    private void verifyTableModFailedBadTable(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.TABLE_MOD_FAILED,
                ECodeTableModFailed.BAD_TABLE);
    }

    @Test
    public void errorTableModFailedBadTable11(){
        print(EOL + "errorTableModFailedBadTable11()");
        verifyTableModFailedBadTable(V_1_1);
    }

    @Test
    public void errorTableModFailedBadTable12(){
        print(EOL + "errorTableModFailedBadTable12()");
        verifyTableModFailedBadTable(V_1_2);
    }

    @Test
    public void errorTableModFailedBadTable13(){
        print(EOL + "errorTableModFailedBadTable13()");
        verifyTableModFailedBadTable(V_1_3);
    }

    // =========================================================
    private void verifyQueueOpFailedBadPort(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.QUEUE_OP_FAILED,
                ECodeQueueOpFailed.BAD_PORT);
    }

    @Test
    public void errorQueueOpFailedBadPort10(){
        print(EOL + "errorQueueOpFailedBadPort10()");
        verifyQueueOpFailedBadPort(V_1_0);
    }

    @Test
    public void errorQueueOpFailedBadPort11(){
        print(EOL + "errorQueueOpFailedBadPort11()");
        verifyQueueOpFailedBadPort(V_1_1);
    }

    @Test
    public void errorQueueOpFailedBadPort12(){
        print(EOL + "errorQueueOpFailedBadPort12()");
        verifyQueueOpFailedBadPort(V_1_2);
    }

    @Test
    public void errorQueueOpFailedBadPort13(){
        print(EOL + "errorQueueOpFailedBadPort13()");
        verifyQueueOpFailedBadPort(V_1_3);
    }

    // =========================================================
    private void verifySwitchConfigFailedBadFlags(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.SWITCH_CONFIG_FAILED,
                ECodeSwitchConfigFailed.BAD_FLAGS);
    }

    @Test
    public void errorSwitchConfigFailedBadFlags11(){
        print(EOL + "errorSwitchConfigFailedBadFlags11()");
        verifySwitchConfigFailedBadFlags(V_1_1);
    }

    @Test
    public void errorSwitchConfigFailedBadFlags12(){
        print(EOL + "errorSwitchConfigFailedBadFlags12()");
        verifySwitchConfigFailedBadFlags(V_1_2);
    }

    @Test
    public void errorSwitchConfigFailedBadFlags13(){
        print(EOL + "errorSwitchConfigFailedBadFlags13()");
        verifySwitchConfigFailedBadFlags(V_1_3);
    }

    // =========================================================
    private void verifyRoleRequestFailedStale(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.ROLE_REQUEST_FAILED,
                ECodeRoleRequestFailed.STALE);
    }

    @Test
    public void errorRoleRequestFailedStale12(){
        print(EOL + "errorRoleRequestFailedStale12()");
        verifyRoleRequestFailedStale(V_1_2);
    }

    @Test
    public void errorRoleRequestFailedStale13(){
        print(EOL + "errorRoleRequestFailedStale13()");
        verifyRoleRequestFailedStale(V_1_3);
    }

    // =========================================================
    private void verifyMeterModFailedUnknown(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.METER_MOD_FAILED,
                ECodeMeterModFailed.UNKNOWN);
    }

    @Test
    public void errorMeterModFailedUnknown13(){
        print(EOL + "errorMeterModFailedUnknown13()");
        verifyMeterModFailedUnknown(V_1_3);
    }

    // =========================================================
    private void verifyTableFeaturesFailedBadLen(ProtocolVersion pv) {
        verifyErrorMessage(pv, ErrorType.TABLE_FEATURES_FAILED,
                ECodeTableFeaturesFailed.BAD_LEN);
    }

    @Test
    public void errorTableFeaturesFailedBadLen13(){
        print(EOL + "errorTableFeaturesFailedBadLen13()");
        verifyTableFeaturesFailedBadLen(V_1_3);
    }

    // ============================================= CREATING / ENCODING ====

    private void verifyEncodingHelloInc(MutableMessage mm,
            ProtocolVersion pv) {
        String file = derivedFile(pv, ErrorType.HELLO_FAILED,
                ECodeHelloFailed.INCOMPATIBLE);

        verifyMutableHeader(mm, pv, ERROR, 0);
        OfmMutableError err = (OfmMutableError) mm;
        err.errorType(ErrorType.HELLO_FAILED);
        err.errorCode(ECodeHelloFailed.INCOMPATIBLE);
        err.errorMessage(EXP_HELLO_INC_MSG);
        encodeAndVerifyMessage(mm.toImmutable(), file);
    }

    @Test
    public void encodeErrorHelloInc13() {
        print(EOL + "encodeErrorHelloInc13()");
        mm = MessageFactory.create(V_1_3, ERROR);
        mm.clearXid();
        verifyEncodingHelloInc(mm, V_1_3);
    }

    @Test
    public void encodeErrorHelloInc12() {
        print(EOL + "encodeErrorHelloInc12()");
        mm = MessageFactory.create(V_1_2, ERROR);
        mm.clearXid();
        verifyEncodingHelloInc(mm, V_1_2);
    }

    @Test
    public void encodeErrorHelloInc11() {
        print(EOL + "encodeErrorHelloInc11()");
        mm = MessageFactory.create(V_1_1, ERROR);
        mm.clearXid();
        verifyEncodingHelloInc(mm, V_1_1);
    }

    @Test
    public void encodeErrorHelloInc10() {
        print(EOL + "encodeErrorHelloInc10()");
        mm = MessageFactory.create(V_1_0, ERROR);
        mm.clearXid();
        verifyEncodingHelloInc(mm, V_1_0);
    }

    //===============================================================
    private void verifyEncodingErrorMessage(ProtocolVersion pv,
            ErrorType type, ErrorCode code) {
        String file = derivedFile(pv, type, code);
        mm = MessageFactory.create(pv, ERROR);
        mm.clearXid();
        verifyMutableHeader(mm, pv, ERROR, 0);
        OfmMutableError err = (OfmMutableError) mm;
        err.errorType(type);
        err.errorCode(code);
        err.setData(FAKE_DATA);
        encodeAndVerifyMessage(mm.toImmutable(), file);
    }

    //===============================================================
    private void verifyEncodingBadReqBuffEmpty(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.BAD_REQUEST,
                ECodeBadRequest.BUFFER_EMPTY);
    }

    @Test
    public void encodeErrorBadReqBuffEmpty13() {
        print(EOL + "encodeErrorBadReqBuffEmpty13()");
        verifyEncodingBadReqBuffEmpty(V_1_3);
    }

    @Test
    public void encodeErrorBadReqBuffEmpty12() {
        print(EOL + "encodeErrorBadReqBuffEmpty12()");
        verifyEncodingBadReqBuffEmpty(V_1_2);
    }

    @Test
    public void encodeErrorBadReqBuffEmpty11() {
        print(EOL + "encodeErrorBadReqBuffEmpty11()");
        verifyEncodingBadReqBuffEmpty(V_1_1);
    }

    @Test
    public void encodeErrorBadReqBuffEmpty10() {
        print(EOL + "encodeErrorBadReqBuffEmpty10()");
        verifyEncodingBadReqBuffEmpty(V_1_0);
    }

    //===============================================================
    private void verifyEncodingBadActionBadQueue(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.BAD_ACTION,
                ECodeBadAction.BAD_QUEUE);
    }

    @Test
    public void encodeErrorBadActionBadQueue13() {
        print(EOL + "encodeErrorBadActionBadQueue13()");
        verifyEncodingBadActionBadQueue(V_1_3);
    }

    @Test
    public void encodeErrorBadActionBadQueue12() {
        print(EOL + "encodeErrorBadActionBadQueue12()");
        verifyEncodingBadActionBadQueue(V_1_2);
    }

    @Test
    public void encodeErrorBadActionBadQueue11() {
        print(EOL + "encodeErrorBadActionBadQueue11()");
        verifyEncodingBadActionBadQueue(V_1_1);
    }

    @Test
    public void encodeErrorBadActionBadQueue10() {
        print(EOL + "encodeErrorBadActionBadQueue10()");
        verifyEncodingBadActionBadQueue(V_1_0);
    }

    //===============================================================
    private void verifyEncodingBadInstructionUnsupMetadata(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.BAD_INSTRUCTION,
                ECodeBadInstruction.UNSUP_METADATA);
    }

    @Test
    public void encodeErrorBadInstructionUnsupMetadata13() {
        print(EOL + "encodeErrorBadInstructionUnsupMetadata13()");
        verifyEncodingBadInstructionUnsupMetadata(V_1_3);
    }

    @Test
    public void encodeErrorBadInstructionUnsupMetadata12() {
        print(EOL + "encodeErrorBadInstructionUnsupMetadata12()");
        verifyEncodingBadInstructionUnsupMetadata(V_1_2);
    }

    @Test
    public void encodeErrorBadInstructionUnsupMetadata11() {
        print(EOL + "encodeErrorBadInstructionUnsupMetadata11()");
        verifyEncodingBadInstructionUnsupMetadata(V_1_1);
    }

    //===============================================================
    private void verifyEncodingBadMatchbadValue(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.BAD_MATCH,
                ECodeBadMatch.BAD_VALUE);
    }

    @Test
    public void encodeErrorBadMatchBadValue13() {
        print(EOL + "encodeErrorBadMatchBadValue13()");
        verifyEncodingBadMatchbadValue(V_1_3);
    }

    @Test
    public void encodeErrorBadMatchBadValue12() {
        print(EOL + "encodeErrorBadMatchBadValue12()");
        verifyEncodingBadMatchbadValue(V_1_2);
    }

    @Test
    public void encodeErrorBadMatchBadValue11() {
        print(EOL + "encodeErrorBadMatchBadValue11()");
        verifyEncodingBadMatchbadValue(V_1_1);
    }

    //===============================================================
    private void verifyEncodingFlowModFailedOverlap(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.FLOW_MOD_FAILED,
                ECodeFlowModFailed.OVERLAP);
    }

    @Test
    public void encodeErrorFlowModFailedOverlap13() {
        print(EOL + "encodeErrorFlowModFailedOverlap13()");
        verifyEncodingFlowModFailedOverlap(V_1_3);
    }

    @Test
    public void encodeErrorFlowModFailedOverlap12() {
        print(EOL + "encodeErrorFlowModFailedOverlap12()");
        verifyEncodingFlowModFailedOverlap(V_1_2);
    }

    @Test
    public void encodeErrorFlowModFailedOverlap11() {
        print(EOL + "encodeErrorFlowModFailedOverlap11()");
        verifyEncodingFlowModFailedOverlap(V_1_1);
    }

    @Test
    public void encodeErrorFlowModFailedOverlap10() {
        print(EOL + "encodeErrorFlowModFailedOverlap10()");
        verifyEncodingFlowModFailedOverlap(V_1_0);
    }

    //===============================================================
    private void verifyEncodingGroupModFailedLoop(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.GROUP_MOD_FAILED,
                ECodeGroupModFailed.LOOP);
    }

    @Test
    public void encodeErrorGroupModFailedLoop13() {
        print(EOL + "encodeErrorGroupModFailedLoop13()");
        verifyEncodingGroupModFailedLoop(V_1_3);
    }

    @Test
    public void encodeErrorGroupModFailedLoop12() {
        print(EOL + "encodeErrorGroupModFailedLoop12()");
        verifyEncodingGroupModFailedLoop(V_1_2);
    }

    @Test
    public void encodeErrorGroupModFailedLoop11() {
        print(EOL + "encodeErrorGroupModFailedLoop11()");
        verifyEncodingGroupModFailedLoop(V_1_1);
    }

    //===============================================================
    private void verifyEncodingPortModFailedBadPort(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.PORT_MOD_FAILED,
                ECodePortModFailed.BAD_PORT);
    }

    @Test
    public void encodeErrorPortModFailedBadPort13() {
        print(EOL + "encodeErrorPortModFailedBadPort13()");
        verifyEncodingPortModFailedBadPort(V_1_3);
    }

    @Test
    public void encodeErrorPortModFailedBadPort12() {
        print(EOL + "encodeErrorPortModFailedBadPort12()");
        verifyEncodingPortModFailedBadPort(V_1_2);
    }

    @Test
    public void encodeErrorPortModFailedBadPort11() {
        print(EOL + "encodeErrorPortModFailedBadPort11()");
        verifyEncodingPortModFailedBadPort(V_1_1);
    }

    //===============================================================
    private void verifyEncodingTableModFailedBadTable(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.TABLE_MOD_FAILED,
                ECodeTableModFailed.BAD_TABLE);
    }

    @Test
    public void encodeErrorTableModFailedBadTable13() {
        print(EOL + "encodeErrorTableModFailedBadTable13()");
        verifyEncodingTableModFailedBadTable(V_1_3);
    }

    @Test
    public void encodeErrorTableModFailedBadTable12() {
        print(EOL + "encodeErrorTableModFailedBadTable12()");
        verifyEncodingTableModFailedBadTable(V_1_2);
    }

    @Test
    public void encodeErrorTableModFailedBadTable11() {
        print(EOL + "encodeErrorTableModFailedBadTable11()");
        verifyEncodingTableModFailedBadTable(V_1_1);
    }

    //===============================================================
    private void verifyEncodingQueueOpFailedBadPort(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.QUEUE_OP_FAILED,
                ECodeQueueOpFailed.BAD_PORT);
    }

    @Test
    public void encodeErrorQueueOpBadTable13() {
        print(EOL + "encodeErrorQueueOpBadTable13()");
        verifyEncodingQueueOpFailedBadPort(V_1_3);
    }

    @Test
    public void encodeErrorQueueOpBadTable12() {
        print(EOL + "encodeErrorQueueOpBadTable12()");
        verifyEncodingQueueOpFailedBadPort(V_1_2);
    }

    @Test
    public void encodeErrorQueueOpBadTable11() {
        print(EOL + "encodeErrorQueueOpBadTable11()");
        verifyEncodingQueueOpFailedBadPort(V_1_1);
    }

    //===============================================================
    private void verifyEncodingSwitchConfigFailedBadFlags(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.SWITCH_CONFIG_FAILED,
                ECodeSwitchConfigFailed.BAD_FLAGS);
    }

    @Test
    public void encodeErrorSwitchConfigFailedBadFlags13() {
        print(EOL + "encodeErrorSwitchConfigFailedBadFlags13()");
        verifyEncodingSwitchConfigFailedBadFlags(V_1_3);
    }

    @Test
    public void encodeErrorSwitchConfigFailedBadFlags12() {
        print(EOL + "encodeErrorSwitchConfigFailedBadFlags12()");
        verifyEncodingSwitchConfigFailedBadFlags(V_1_2);
    }

    @Test
    public void encodeErrorSwitchConfigFailedBadFlags11() {
        print(EOL + "encodeErrorSwitchConfigFailedBadFlags11()");
        verifyEncodingSwitchConfigFailedBadFlags(V_1_1);
    }

    //===============================================================
    private void verifyEncodingRoleRequestFailedStale(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.ROLE_REQUEST_FAILED,
                ECodeRoleRequestFailed.STALE);
    }

    @Test
    public void encodeErrorRoleRequestFailedStale13() {
        print(EOL + "encodeErrorRoleRequestFailedStale13()");
        verifyEncodingRoleRequestFailedStale(V_1_3);
    }

    @Test
    public void encodeErrorRoleRequestFailedStale12() {
        print(EOL + "encodeErrorRoleRequestFailedStale12()");
        verifyEncodingRoleRequestFailedStale(V_1_2);
    }

    //===============================================================
    private void verifyEncodingMeterModFailedUnknown(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.METER_MOD_FAILED,
                ECodeMeterModFailed.UNKNOWN);
    }

    @Test
    public void encodeErrorMeterModFailedUnknown13() {
        print(EOL + "encodeErrorMeterModFailedUnknown13()");
        verifyEncodingMeterModFailedUnknown(V_1_3);
    }

    //===============================================================
    private void verifyEncodingTableFeaturesFailedBadLen(ProtocolVersion pv) {
        verifyEncodingErrorMessage(pv, ErrorType.TABLE_FEATURES_FAILED,
                ECodeTableFeaturesFailed.BAD_LEN);
    }

    @Test
    public void encodeErrorTableFeaturesFailedBadLen13() {
        print(EOL + "encodeErrorTableFeaturesFailedBadLen13()");
        verifyEncodingTableFeaturesFailedBadLen(V_1_3);
    }

    //===============================================================
}
