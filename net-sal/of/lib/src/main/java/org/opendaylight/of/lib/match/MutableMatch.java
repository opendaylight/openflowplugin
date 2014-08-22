/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.*;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.ValidationException;

import java.util.*;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.MatchUtils.sameMatchFields;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;
import static org.opendaylight.of.lib.match.MatchFactory.FIELD_HEADER_LEN;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;

/**
 * Mutable subclass of {@link Match}.
 *
 * @author Simon Hunt
 */
public class MutableMatch extends Match implements MutableStructure {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MutableMatch.class, "mutableMatch");

    private static final String E_DUP = RES.getString("e_dup");

    private final Mutable mutt = new Mutable();
    private final Map<OxmFieldType, MatchField> pendingFields =
            new TreeMap<OxmFieldType, MatchField>();

    /**
     * Constructs a mutable match structure.
     *
     * @param pv     the protocol version
     * @param header the match header
     */
    MutableMatch(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    /**
     * {@inheritDoc}
     * <p>
     * In addition, the match fields are validated to ensure that all their
     * pre-requisites are met.
     *
     * @return an immutable instance of this structure.
     * @throws ValidationException if match field pre-requisites have not
     *          been met
     * @throws InvalidMutableException if this structure is no longer writable
     */
    @Override
    public OpenflowStructure toImmutable() {
        // transfer fields from pending map...
        fields.addAll(fieldList());
        // now validate
        validateFields();

        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        Match match = new Match(version, header);
        match.fields.addAll(fields);
        return match;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }

    @Override
    List<MatchField> fieldList() {
        return Collections.unmodifiableList(
                new ArrayList<MatchField>(pendingFields.values()));
    }

    @Override
    public List<MatchField> getMatchFields() {
        return fieldList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutableMatch mm = (MutableMatch) o;
        return header.equals(mm.header) &&
                sameMatchFields(fieldList(), mm.fieldList());
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + pendingFields.hashCode();
        return result;
    }


    // =====================================================================
    // ==== SETTERS


    /** Adds the specified match field to this match. Note that the order in
     * which the match fields are added to the match may be arbitrary.
     * An exception will be thrown if a field type is duplicated.
     *
     * @param field the match field to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if field is null
     * @throws IllegalArgumentException if the match field type is duplicated
     */
    public MutableMatch addField(MatchField field) {
        mutt.checkWritable(this);
        notNull(field);
        final ProtocolVersion pv = version;
        // Are we allowing the field to be added to this match version...
        OxmBasicFieldType ft = (OxmBasicFieldType) field.getFieldType();
        versionCheck(pv, field.getVersion(), ft);
        if (pendingFields.containsKey(ft))
            throw new IllegalArgumentException(E_DUP + ft);

        pendingFields.put(ft, field);
        // for TLV matches, we need to add the length of the field
        if (pv.ge(V_1_2))
            header.length += FIELD_HEADER_LEN + field.header.length;
        return this;
    }

    /** Encapsulates the validation of the fields. */
    private void validateFields() {
        try {
            MatchValidator.validateMatch(this);
            // good to go
        } catch (ValidationException ve) {
            // remove the field from the list
            fields.remove(fields.size()-1);
            // now throw the exception
            throw ve;
        }
    }


    /** Checks that the given field type is valid in a match, for the
     * specified protocol versions.
     * Silently returns if all is well; throws an exception otherwise.
     *
     * @param pv the protocol version of the match
     * @param fpv the protocol version of the field
     * @param ft the field type to check
     * @throws VersionMismatchException if the field type is not appropriate
     *          for the version
     */
    static void versionCheck(ProtocolVersion pv, ProtocolVersion fpv,
                             OxmBasicFieldType ft) {
        // first, check that the version of the match and field are the same
        if (pv != fpv)
            throw new VersionMismatchException(pv + E_BAD_FIELD_VERSION + fpv);

        boolean bad;
        if (pv.ge(V_1_2)) {
            bad = (pv == V_1_2 && ft.getCode() > V12_MAX);
        } else {
            Set<OxmBasicFieldType> fieldSet = (pv == V_1_0) ? FIELDS_10_SET
                                                            : FIELDS_11_SET;
            bad = !fieldSet.contains(ft);
        }
        if (bad)
            throw new VersionMismatchException(pv + E_BAD_MATCH_FIELD + ft);
    }

    private static final String E_BAD_MATCH_FIELD = RES
            .getString("e_bad_match_field");
    private static final String E_BAD_FIELD_VERSION = RES
            .getString("e_bad_field_version");

    // === define the match fields that are valid for legacy versions
    // TODO: Review - care needs to be taken: ARP_OP and IP_PROTO share field

    private static final Set<OxmBasicFieldType> FIELDS_10_SET =
            new HashSet<OxmBasicFieldType>(Arrays.asList(
                    IN_PORT, ETH_DST, ETH_SRC, ETH_TYPE, VLAN_VID, VLAN_PCP,
                    IP_DSCP, IP_PROTO, IPV4_SRC, IPV4_DST,
                    TCP_SRC, TCP_DST, UDP_SRC, UDP_DST,
                    ICMPV4_TYPE, ICMPV4_CODE, ARP_OP
            ));

    private static final Set<OxmBasicFieldType> FIELDS_11_SET =
            new HashSet<OxmBasicFieldType>(Arrays.asList(
                    IN_PORT, METADATA, ETH_DST, ETH_SRC, ETH_TYPE, VLAN_VID,
                    VLAN_PCP, IP_DSCP, IP_PROTO, IPV4_SRC, IPV4_DST,
                    TCP_SRC, TCP_DST, UDP_SRC, UDP_DST, SCTP_SRC, SCTP_DST,
                    ICMPV4_TYPE, ICMPV4_CODE, ARP_OP, MPLS_LABEL, MPLS_TC
            ));
}