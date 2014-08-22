/*
 * (c) Copyright 2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.format;

/**
 * Auxiliary interface through which arbitrary string values can be translated
 * into alternate values.
 * 
 * @author Thomas Vachuska
 */
public abstract interface TokenTranslator {
    
    /**
     * Returns a replacement string for the original token string.
     * 
     * @param string original token string
     * @return replacement string
     */
    public String translate (String string);
    
}
