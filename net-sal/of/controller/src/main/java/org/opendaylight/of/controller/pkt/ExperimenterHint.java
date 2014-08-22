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

/**
 * A default implementation of an experimenter-defined {@link Hint}.
 * <p>
 * Experimenters may use this class as is, defining their own hint types
 * (using negative integers), and payloads. Alternatively,
 * they may extend this class to provide a richer API on the subclass,
 * tailored to specific payloads.
 *
 * @author Simon Hunt
 */
public class ExperimenterHint extends AbstractHint {

    /** Exception message for non-negative experimenter hint types. */
    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            ExperimenterHint.class, "experimenterHint");

    public static final String E_NON_NEGATIVE_TYPE = RES
            .getString("e_non_negative_type");

    private final Payload payload;

    /** Constructs an experimenter hint, using the given experimenter-defined
     * type and payload. Note that the hint type must be a negative number,
     * since positive numbers are reserved for the {@link HintType standard}
     * hint types.
     *
     * @param experType the experimenter-defined hint type
     * @param payload the hint payload
     * @throws IllegalArgumentException if experType is not negative
     */
    public ExperimenterHint(int experType, Payload payload) {
        super(experType);
        if (experType >= 0)
            throw new IllegalArgumentException(E_NON_NEGATIVE_TYPE + experType);
        this.payload = payload;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",payload=").append(payload).append("}");
        return sb.toString();
    }

    /** Returns the payload.
     *
     * @return the payload
     */
    public Payload getPayload() {
        return payload;
    }

    /** A tag interface to allow the hint payload to be expressed in general
     * terms. This provides experimenters with a way to define their own
     * payload types, to be used with this class.
     * <p>
     * It is <strong>strongly</strong> recommended that payload instances
     * are immutable, to guarantee the immutability of the hint as a whole.
     */
    public interface Payload { }
}