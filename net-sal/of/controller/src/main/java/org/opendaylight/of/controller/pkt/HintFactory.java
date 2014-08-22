/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

import org.opendaylight.util.ResourceUtils;

import java.util.ResourceBundle;

import static org.opendaylight.of.controller.pkt.HintType.HANDLER;
import static org.opendaylight.of.controller.pkt.HintType.TEST_PACKET;
import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Provides facilities for creating {@link Hint} instances.
 *
 * @author Simon Hunt
 */
public class HintFactory {

    // list of "type-only" hints for validation of the createHint() method call
    private static final HintType[] TYPE_ONLY_HINTS = {
            TEST_PACKET
    };

    // No instantiation
    private HintFactory() {}

    // =====================================================================
    // === Create Hints

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            HintFactory.class, "hintFactory");

    private static final String E_UNEX_TYPE = RES.getString("e_unex_type");

    /** Validates that the given hint type is one of the specified valid types.
     *
     * @param given the type that was given
     * @param validTypes all the valid types for the current call
     */
    private static void checkType(HintType given, HintType... validTypes) {
        boolean good = false;
        for (HintType type: validTypes)
            if (given == type) {
                good = true;
                break;
            }
        if (!good)
            throw new IllegalArgumentException(E_UNEX_TYPE + given);
    }

    // =====================================================================
    // === STANDARD hint types

    /** Creates a hint with the specified type. This method is used to create
     * hint types that have no additional context data. The currently supported
     * types are:
     * <ul>
     *     <li>{@link HintType#TEST_PACKET TEST_PACKET}</li>
     * </ul>
     *
     * @param type the hint type
     * @return the hint
     */
    public static Hint createHint(HintType type) {
        notNull(type);
        checkType(type, TYPE_ONLY_HINTS);
        return new TypeOnlyHint(type);
    }

    /** Creates a hint that describes the implementing class of a packet
     * listener.
     *
     * @param type must be HintType.HANDLER
     * @param cls the packet listener class
     * @return the hint
     */
    public static Hint
    createHint(HintType type, Class<? extends SequencedPacketListener> cls) {
        notNull(type, cls);
        checkType(type, HANDLER);
        return new HandlerHint(cls);
    }

    // =====================================================================
    // === EXPERIMENTER hint types

    /** Creates an experimenter hint for the specified experimenter-defined
     * type. Note that experimenter-defined types must be negative.
     *
     * @param experType the experimenter-defined type
     * @return the experimenter hint
     */
    public static Hint createHint(int experType) {
        return createHint(experType, null);
    }

    /** Creates an experimenter hint for the specified experimenter-defined
     * type and given payload.
     * Note that experimenter-defined types must be negative.
     *
     * @param experType the experimenter-defined type
     * @param payload the experimenter payload
     * @return the experimenter hint
     */
    public static Hint createHint(int experType,
                                  ExperimenterHint.Payload payload) {
        return new ExperimenterHint(experType, payload);
    }
}