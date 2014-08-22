/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline;

import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.match.OxmFieldType;
import org.opendaylight.of.lib.msg.TableFeaturePropType;

import java.util.Set;

/**
 * Encapsulates table properties and associated capabilities.
 * <p> The capabilities can be categorized as follows :<br>
 * <em>1. capabilities</em> : All the supported basic capabilities.<br>
 * <em>2. match capabilities</em> : All the supported capabilities involving
 * <em>matchfield</em>.<br>
 * <em>3. match fields</em> : All the supported match fields along with mask.
 *
 * @author Pramod Shanbhag
 */
public interface TableContext {

    /** Returns the table ID.
     *
     * @return the table ID
     */
    TableId tableId();

    /** Returns the table Id of the next supported table in the pipeline
     * for a table miss rule.
     *
     * @return the the next table Id for a table miss rule.
     */
    public TableId getNextTableMiss();

    /** Returns true if the context supports next table in the pipeline
     * for a table miss rule.
     *
     * @return true if the context supports next table for table miss rule.
     */
    public boolean hasNextTablesMiss();

    /** Returns true if the given table is present in next tables set.
     *
     * @param id the table Id
     * @return true if the given Id is present in next table set
     */
    public boolean containsNextTable(TableId id);

    /** Returns true if the given table is present in next tables miss set.
     *
     * @param id the table Id
     * @return true if the given Id is present in next table miss set
     */
    public boolean containsNextTableMiss(TableId id);

    /** Returns the max entries supported by the table.
     *
     * @return the max supported entries.
     */
    public long maxEntries();

    /** Returns true if the context supports given capability-code combination.
     *
     * @param prop the given table feature prop type
     * @param code the given capability code
     * @return true if the context supports given capability-code combination.
     */
    public boolean supportsCapability(TableFeaturePropType prop,
                                 OfpCodeBasedEnum code);

    /** Returns true if the context supports given match capability-code
     * combination.
     *
     * @param prop the given table feature prop type
     * @param code the given capability code
     * @return true if the context supports given match capability-code
     *          combination.
     */
    public boolean supportsMatchFieldCapability(TableFeaturePropType prop,
                                           OxmFieldType code);

    /** Returns true if the context supports given match field.
     * The method also validates the support for masking if the given match
     * field has mask bit set.
     *
     * @param mf the match field
     * @return true if the context supports given match field
     */
    public boolean supportsMatchField(MatchField mf);

    /** Returns true if the context supports wild carding all of the given
     * match field types.
     *
     * @param fmmf set of match field types
     * @return true if the context supports wild carding all of the given
     * match field types
     */
    public boolean supportsWildCards(Set<OxmFieldType> fmmf);
}
