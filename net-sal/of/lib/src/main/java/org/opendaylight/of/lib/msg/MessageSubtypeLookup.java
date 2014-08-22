/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.util.ResourceUtils;

import java.util.ResourceBundle;

/**
 * Provides a validation service for message type and subtype combinations.
 *
 * @author Simon Hunt
 */
class MessageSubtypeLookup {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MessageSubtypeLookup.class, "messageSubtypeLookup");

    private static final String E_BAD_SUB = RES.getString("e_bad_sub");
    private static final String SLASH = " / ";
    private static final String DOT = ".";

    /** Validates the given combination of version, type and subtype. If all
     * is well (or subtype is null), the method returns silently; otherwise
     * an exception is thrown.
     *
     * @param pv the protocol version
     * @param t the message type
     * @param s the message subtype
     * @throws IllegalArgumentException if the pv/type/subtype combo is bad
     */
    static void validate(ProtocolVersion pv, MessageType t, Enum<?> s) {
        if (s != null) {
            switch (t) {
                case ERROR:
                    verify(ErrorType.class, pv, t, s);
                    break;

                case EXPERIMENTER:
                    verify(ExperimenterId.class, pv, t, s);
                    break;

                case PACKET_IN:
                    verify(PacketInReason.class, pv, t, s);
                    break;

                case FLOW_REMOVED:
                    verify(FlowRemovedReason.class, pv, t, s);
                    break;

                case PORT_STATUS:
                    verify(PortReason.class, pv, t, s);
                    break;

                case FLOW_MOD:
                    verify(FlowModCommand.class, pv, t, s);
                    break;

                case GROUP_MOD:
                    verify(GroupModCommand.class, pv, t, s);
                    break;

                case MULTIPART_REQUEST:
                case MULTIPART_REPLY:
                    verify(MultipartType.class, pv, t, s);
                    break;

                case ROLE_REQUEST:
                case ROLE_REPLY:
                    verify(ControllerRole.class, pv, t, s);
                    break;

                case METER_MOD:
                    verify(MeterModCommand.class, pv, t, s);
                    break;

                default:
                    throw new IllegalArgumentException(pv + E_BAD_SUB +
                            combo(t, s));
            }
        }
    }

    private static void verify(Class<?> subtypeClass, ProtocolVersion pv,
                               MessageType type, Enum<?> subtype) {
        if (!subtypeClass.isInstance(subtype))
            throw new IllegalArgumentException(pv + E_BAD_SUB +
                    combo(type, subtype));
    }

    static String combo(MessageType t, Enum<?> s) {
        return t.getClass().getSimpleName() + DOT + t.name() + SLASH +
                s.getClass().getSimpleName() + DOT + s.name();
    }
}