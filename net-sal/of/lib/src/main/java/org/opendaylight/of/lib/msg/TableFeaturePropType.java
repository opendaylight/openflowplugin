/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.opendaylight.of.lib.CommonUtils.verMin13;

/**
 * Designates feature properties of a table; Since 1.3.
 *
 * @author Simon Hunt
 */
public enum TableFeaturePropType implements OfpCodeBasedEnum {
    /** Instructions property; Since 1.3. */
    INSTRUCTIONS(0),
    /** Instructions for table-miss; Since 1.3. */
    INSTRUCTIONS_MISS(1),
    /** Next Table property; Since 1.3. */
    NEXT_TABLES(2),
    /** Next Table for table-miss; Since 1.3. */
    NEXT_TABLES_MISS(3),
    /** Write Actions property; Since 1.3. */
    WRITE_ACTIONS(4),
    /** Write Actions for table-miss; Since 1.3. */
    WRITE_ACTIONS_MISS(5),
    /** Apply Actions property; Since 1.3. */
    APPLY_ACTIONS(6),
    /** Apply Actions for table-miss; Since 1.3. */
    APPLY_ACTIONS_MISS(7),
    /** Match property; Since 1.3. */
    MATCH(8),
    /** Wildcards property; Since 1.3. */
    WILDCARDS(10),
    /** Write Set-Field property; Since 1.3. */
    WRITE_SETFIELD(12),
    /** Write Set-Field for table-miss; Since 1.3. */
    WRITE_SETFIELD_MISS(13),
    /** Apply Set-Field property; Since 1.3. */
    APPLY_SETFIELD(14),
    /** Apply Set-Field for table-miss; Since 1.3. */
    APPLY_SETFIELD_MISS(15),
    /** Experimenter property; Since 1.3. */
    EXPERIMENTER(0xfffe),
    /** Experimenter for table-miss; Since 1.3. */
    EXPERIMENTER_MISS(0xffff),
    ;

    private final int code;

    TableFeaturePropType(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /**
     * Convenient access to the regular type for this property type.
     * For non-miss prop types it will be itself, as it is already a regular
     * property type.
     *
     * @return the regular property type
     */
    public TableFeaturePropType regular() {
        if (!isMiss())
            return this;

        try {
            return decode(code - 1, ProtocolVersion.V_1_3);
        } catch (DecodeException e) {
            // cannot happen ... trust me
            return null;
        }
    }

    /**
     * Returns true if this type constant is one of the MISS variants.
     *
     * @return true if represents a _MISS property type
     */
    public boolean isMiss() {
        return code % 2 != 0;
    }

    static TableFeaturePropType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        verMin13(pv);
        TableFeaturePropType type = null;
        for (TableFeaturePropType t: values())
            if (t.code == code) {
                type = t;
                break;
            }
        if (type == null)
            throw new DecodeException("TableFeaturePropType: unknown code: " +
                                        code);

        // no further version constraints
        return type;
    }
}
