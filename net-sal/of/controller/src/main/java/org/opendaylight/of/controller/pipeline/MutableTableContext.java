/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline;

import org.opendaylight.of.lib.MutableObject;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.OxmFieldType;
import org.opendaylight.of.lib.msg.TableFeaturePropType;

import java.util.Set;

/**
 * Mutable variant of {@link TableContext}. Provides modification capability
 * to the context.
 *
 * @author Pramod Shanbhag
 */
public interface MutableTableContext
        extends MutableType<TableContext>, MutableObject {

    /** Sets the table Id in the context.
     *
     * @param id the tableId
     * @return self, for chaining
     */
    public MutableTableContext tableId(TableId id);

    /** Sets the maxEntries in the context
     *
     * @param max the max entries
     * @return self, for chaining
     */
    public MutableTableContext maxEntries(long max);

    /** Adds the given table Id to the next tables set.
     *
     * @param id the table Id
     * @return self, for chaining
     */
    public MutableTableContext addNextTable(TableId id);

    /** Adds the given table Id to the next tables miss set.
     *
     * @param id the table Id
     * @return self, for chaining
     */
    public MutableTableContext addNextTableMiss(TableId id);

    /** Adds the given capability to the context.
     *
     * @param prop the prop type of the capability
     * @param caps set of supported capability codes for this capability
     * @return self, for chaining
     */
    public MutableTableContext addCapability(TableFeaturePropType prop,
                                             Set<? extends OfpCodeBasedEnum> caps);

    /** Adds the given match capability to the context.
     *
     * @param prop the prop type of the capability
     * @param caps set of supported capability codes for this capability
     * @return self, for chaining
     */
    public MutableTableContext addMatchFieldCapability(TableFeaturePropType prop,
                                             Set<? extends OxmFieldType> caps);

    /** Adds the given match field to the context
     *
     * @param ft the given match field type
     * @param hasMask given field mask bit
     * @return self, for chaining
     */
    public MutableTableContext addMatchField(OxmFieldType ft, boolean hasMask);

}
