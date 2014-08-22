/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.match.MFieldExperimenter;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.util.ResourceUtils;

import java.util.*;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.util.StringUtils.spaces;

/**
 * Represents an "OXM" table feature property. This implementation provides the
 * data as a set of supported {@link OxmBasicFieldType OXM basic field types},
 * with a lookup indicating whether the {@link #hasMaskBitSet has-mask} bit
 * was set. Additionally, if any are defined, a list of supported
 * {@link MFieldExperimenter experimenter fields} are provided.
 *
 * @author Simon Hunt
 */
public class TableFeaturePropOxm extends TableFeatureProp {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            TableFeaturePropOxm.class, "tableFeaturePropOxm");

    private static final String E_FIELD_NOT_PRESENT = RES
            .getString("e_field_not_present");

    Map<OxmBasicFieldType, Boolean> fieldAndMask;
    List<MFieldExperimenter> experFields;

    /**
     * Constructs a table feature property.
     *
     * @param header the property header
     */
    TableFeaturePropOxm(Header header) {
        super(header);
    }

    /** Returns true if the mask bit was set for the specified field type.
     * If the specified field type was not present in the table feature
     * property, an exception is thrown.
     *
     * @param ft the field type to look up
     * @return true if the has-mask bit was set; false otherwise
     * @throws NullPointerException if ft is null
     * @throws IllegalArgumentException if the field is not present in this
     *          feature property
     */
    public boolean hasMaskBitSet(OxmBasicFieldType ft) {
        notNull(ft);
        Boolean bool = fieldAndMask.get(ft);
        if (bool == null)
            throw new IllegalArgumentException(E_FIELD_NOT_PRESENT + ft);
        return bool;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        final int len = sb.length();
        sb.replace(len - 1, len, ": supported=").append(fieldAndMask.keySet());
        if (experFields.size() > 0)
            sb.append(",exper=").append(experFields);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int getTotalLength() {
        return header.length;
    }

    @Override
    String toDebugString(int indent) {
        StringBuilder sb = new StringBuilder(super.toDebugString(indent));
        final String indStr = EOLI + spaces(indent + 2);
        final String indStr2 = indStr + "  ";
        sb.append(indStr);
        if (fieldAndMask.size() == 0) {
            sb.append("(none)");
        } else {
            for (OxmBasicFieldType ft: fieldAndMask.keySet()) {
                String m = hasMaskBitSet(ft) ? "{maskable}, " : ", ";
                sb.append(ft).append(m);
            }
            final int len = sb.length();
            sb.replace(len-2, len, "");
        }
        if (experFields != null && experFields.size() > 0) {
            sb.append(indStr).append("Experimenter:");
            for (MFieldExperimenter f: experFields)
                sb.append(indStr2).append(f);
        }
        return sb.toString();
    }

    /** Returns the set of OXM basic match field types supported by this
     * table feature; Since 1.3.
     *
     * @return the set of supported basic match field types
     */
    public Set<OxmBasicFieldType> getSupportedFieldTypes() {
        return Collections.unmodifiableSet(fieldAndMask.keySet());
    }

    /** Returns the list of experimenter match fields supported by this
     * table feature; Since 1.3.
     *
     * @return the list of supported experimenter match fields
     */
    public List<MFieldExperimenter> getSupportedExperFields() {
        return Collections.unmodifiableList(experFields);
    }
}