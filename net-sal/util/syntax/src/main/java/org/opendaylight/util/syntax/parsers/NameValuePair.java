/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;


/**
 * Simple data carrier for name-value pairs specified on the command-line.
 *
 * @author Thomas Vachuska
 */
public class NameValuePair implements Serializable {
    
    private static final long serialVersionUID = -3065552703796890141L;

    private String name;
    private Serializable value;
    
    /**
     * Default constructor required for serialization.
     *
     */
    public NameValuePair() {
    }
    
    /**
     * Constructs a name value pair.
     * @param name name portion
     * @param value value portion
     */
    public NameValuePair(String name, Serializable value) {
        this.name = name;
        this.value = value;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return Returns the value.
     */
    public Serializable getValue() {
        return value;
    }
    
}
