/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;

import org.opendaylight.of.controller.pipeline.TableAttribute;

import java.util.Set;
import java.util.TreeSet;

/**
 * Default implementation for {@link TableAttribute} based on
 * <em>TableFeatures</em> OpenFlow message.
 * <p>
 * An instance of this class represents a table capability, by the given 
 * {@link TableAttributeCode}.
 * <p>
 * An instance may have child attributes.
 * 
 * @author Pramod Shanbhag
 */
public class DefaultTableAttribute 
    implements TableAttribute, Comparable<DefaultTableAttribute> {

    public final TableAttributeCode code;
    // FIXME : to remove if not used.
    private final Set<TableAttribute> children = new TreeSet<TableAttribute>();

    public DefaultTableAttribute(TableAttributeCode code) {
        this.code = code;
    }
    @Override
    public String name() {
        return code.name();
    }

    @Override
    public String toString() {
        return code.name();
    }
    
    @Override
    public Set<TableAttribute> children() {
        return new TreeSet<TableAttribute>(children);
    }

    @Override
    public boolean hasChildren() {
        return (children.size() > 0);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        //IMPLEMENTATION NOTE : not considering children at this moment.
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultTableAttribute other = (DefaultTableAttribute) obj;
        if (code != other.code)
            return false;
        return true;
    }
    
    @Override
    public int compareTo(DefaultTableAttribute o) {
        return this.code.ordinal() - o.code.ordinal();
    }
}
