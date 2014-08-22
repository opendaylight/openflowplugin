/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.err;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Provides the facility to look up an error code based on a specified
 * error type.
 *
 * @author Simon Hunt
 */
public class ErrorCodeLookup {

    /** No instantiation. */
    private ErrorCodeLookup() {}

    /** Looks up the error code constant for the given encoded value, based
     * on the supplied high-level error type.
     *
     * @param type the error type
     * @param code the error code value to look up
     * @param pv the protocol version
     * @return the appropriate error code constant
     * @throws DecodeException if the code value cannot be decoded
     */
    public static ErrorCode lookup(ErrorType type, int code,
                                   ProtocolVersion pv) throws DecodeException {
        notNull(type);
        switch (type) {

            case HELLO_FAILED:
                return ECodeHelloFailed.decode(code, pv);

            case BAD_REQUEST:
                return ECodeBadRequest.decode(code, pv);

            case BAD_ACTION:
                return ECodeBadAction.decode(code, pv);

            case BAD_INSTRUCTION:
                return ECodeBadInstruction.decode(code, pv);

            case BAD_MATCH:
                return ECodeBadMatch.decode(code, pv);

            case FLOW_MOD_FAILED:
                return ECodeFlowModFailed.decode(code, pv);

            case GROUP_MOD_FAILED:
                return ECodeGroupModFailed.decode(code, pv);

            case PORT_MOD_FAILED:
                return ECodePortModFailed.decode(code, pv);

            case TABLE_MOD_FAILED:
                return ECodeTableModFailed.decode(code, pv);

            case QUEUE_OP_FAILED:
                return ECodeQueueOpFailed.decode(code, pv);

            case SWITCH_CONFIG_FAILED:
                return ECodeSwitchConfigFailed.decode(code, pv);

            case ROLE_REQUEST_FAILED:
                return ECodeRoleRequestFailed.decode(code, pv);

            case METER_MOD_FAILED:
                return ECodeMeterModFailed.decode(code, pv);

            case TABLE_FEATURES_FAILED:
                return ECodeTableFeaturesFailed.decode(code, pv);

            case EXPERIMENTER:
                throw new IllegalStateException();
        }
        return null;
    }

    /** Returns the class of error code for the given error type.
     *
     * @param type the error type
     * @return the class of the corresponding error codes
     */
    public static Class<? extends ErrorCode> codeClass(ErrorType type) {
        notNull(type);
        switch (type) {

            case HELLO_FAILED:
                return ECodeHelloFailed.class;

            case BAD_REQUEST:
                return ECodeBadRequest.class;

            case BAD_ACTION:
                return ECodeBadAction.class;

            case BAD_INSTRUCTION:
                return ECodeBadInstruction.class;

            case BAD_MATCH:
                return ECodeBadMatch.class;

            case FLOW_MOD_FAILED:
                return ECodeFlowModFailed.class;

            case GROUP_MOD_FAILED:
                return ECodeGroupModFailed.class;

            case PORT_MOD_FAILED:
                return ECodePortModFailed.class;

            case TABLE_MOD_FAILED:
                return ECodeTableModFailed.class;

            case QUEUE_OP_FAILED:
                return ECodeQueueOpFailed.class;

            case SWITCH_CONFIG_FAILED:
                return ECodeSwitchConfigFailed.class;

            case ROLE_REQUEST_FAILED:
                return ECodeRoleRequestFailed.class;

            case METER_MOD_FAILED:
                return ECodeMeterModFailed.class;

            case TABLE_FEATURES_FAILED:
                return ECodeTableFeaturesFailed.class ;

            case EXPERIMENTER:
                throw new IllegalStateException();
        }
        return null;
    }

    /** Validates the error type/code combination for the specified
     * protocol version, silently returning if all is well, or throwing
     * an exception if the combination is invalid.
     *
     * @param code the error code
     * @param type the error type
     * @param pv the protocol version
     */
    public static void validate(ErrorType type, ErrorCode code,
                                ProtocolVersion pv) {
        switch (type) {
            case HELLO_FAILED:
                ECodeHelloFailed.validate((ECodeHelloFailed) code, pv);
                break;
            case BAD_REQUEST:
                ECodeBadRequest.validate((ECodeBadRequest) code, pv);
                break;
            case BAD_ACTION:
                ECodeBadAction.validate((ECodeBadAction) code, pv);
                break;
            case BAD_INSTRUCTION:
                ECodeBadInstruction.validate((ECodeBadInstruction) code, pv);
                break;
            case BAD_MATCH:
                ECodeBadMatch.validate((ECodeBadMatch) code, pv);
                break;
            case FLOW_MOD_FAILED:
                ECodeFlowModFailed.validate((ECodeFlowModFailed) code, pv);
                break;
            case GROUP_MOD_FAILED:
                ECodeGroupModFailed.validate((ECodeGroupModFailed) code, pv);
                break;
            case PORT_MOD_FAILED:
                ECodePortModFailed.validate((ECodePortModFailed) code, pv);
                break;
            case TABLE_MOD_FAILED:
                ECodeTableModFailed.validate((ECodeTableModFailed) code, pv);
                break;
            case QUEUE_OP_FAILED:
                ECodeQueueOpFailed.validate((ECodeQueueOpFailed) code, pv);
                break;
            case SWITCH_CONFIG_FAILED:
                ECodeSwitchConfigFailed.validate((ECodeSwitchConfigFailed) code, pv);
                break;
            case ROLE_REQUEST_FAILED:
                ECodeRoleRequestFailed.validate((ECodeRoleRequestFailed) code, pv);
                break;
            case METER_MOD_FAILED:
                ECodeMeterModFailed.validate((ECodeMeterModFailed) code, pv);
                break;
            case TABLE_FEATURES_FAILED:
                ECodeTableFeaturesFailed.validate((ECodeTableFeaturesFailed) code, pv);
                break;
            case EXPERIMENTER:
                // nothing to validate against
                break;
        }
    }
}
