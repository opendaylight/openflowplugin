/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;

/**
 * Portable class.
 * 
 * @author Fabiel Zuniga
 */
public class PortableClass implements Serializable {
    private static final long serialVersionUID = 1L;

    private String attrPreviousVersion;
    private String attrCurrentVersion;

    public PortableClass() {
        this.attrPreviousVersion = "Previous version attribute";
        this.attrCurrentVersion = "Current version attribute";
    }

    /**
     * Gets the value for the previous version attribute.
     *
     * @return previous version attribute's value
     */
    public String getAttrPreviousVersion() {
        return this.attrPreviousVersion;
    }

    /**
     * Sets the value for the previous version attribute.
     *
     * @param attrPreviousVersion previous version attribute's value
     */
    public void setAttrPreviousVersion(String attrPreviousVersion) {
        this.attrPreviousVersion = attrPreviousVersion;
    }

    /**
     * Gets the value for the current version attribute.
     *
     * @return current version attribute's value
     */
    public String getAttrCurrentVersion() {
        return this.attrCurrentVersion;
    }

    /**
     * Sets the value for the current version attribute.
     *
     * @param attrCurrentVersion current version attribute's value
     */
    public void setAttrCurrentVersion(String attrCurrentVersion) {
        this.attrCurrentVersion = attrCurrentVersion;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(getClass().getSimpleName());
        str.append("[attrPreviousVersion=");
        str.append(this.attrPreviousVersion);
        str.append(", attrCurrentVersion=");
        str.append(this.attrCurrentVersion);
        str.append(']');
        return str.toString();
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(this.attrPreviousVersion);
        stream.writeObject(this.attrCurrentVersion);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.attrPreviousVersion = (String) stream.readObject();
        try {
            this.attrCurrentVersion = (String) stream.readObject();
        }
        catch (OptionalDataException e) {
            // An attempt was made to read past the end of data consumable by a class-defined
            // readObject or readExternal method.
            String defaultValueIfNoPresentInOtherVersions = null;
            this.attrCurrentVersion = defaultValueIfNoPresentInOtherVersions;
        }
    }
}
