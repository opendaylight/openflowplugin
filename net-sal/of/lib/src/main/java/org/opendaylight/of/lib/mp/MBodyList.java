/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An abstract base class for {@link MultipartBody} instances that
 * are composed of a list (array) of elements.
 *
 * @param <T> multi-part body type
 *  
 * @author Simon Hunt
 */
public abstract class MBodyList<T extends MultipartBody>
        extends OpenflowStructure implements MultipartBody {

    final List<T> list;

    /** Constructor, initializing the internal list.
     * 
     * @param pv protocol version
     */
    MBodyList(ProtocolVersion pv) {
        super(pv);
        list = new ArrayList<T>();
    }

    @Override
    public ProtocolVersion getVersion() {
       return version;
    }

    @Override
    public int getTotalLength() {
        int total = 0;
        for (T t: list)
            total += t.getTotalLength();
        return total;
    }

    /** Returns the class of the elements of the array.
     *
     * @return the class of the array elements
     */
    public abstract Class<T> getElementClass();

    /** Returns an unmodifiable view of the list.
     *
     * @return the a view of the list
     */
    public List<T> getList() {
        return list == null ? null : Collections.unmodifiableList(list);
    }

    /** Adds the specified contents to the list.
     * Necessary for the mutable arrays to copy across to immutable instances.
     *
     * @param contents the contents to add
     */
    void addAll(List<T> contents) {
        list.addAll(contents);
    }
}
