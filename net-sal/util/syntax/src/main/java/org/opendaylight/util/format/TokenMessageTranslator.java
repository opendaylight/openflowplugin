/*
 * (c) Copyright 2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.format;

/**
 * Formats message strings by translating tokens by their replacement values
 * within the given message string.
 * 
 * @author Thomas Vachuska
 */
public abstract interface TokenMessageTranslator {

    /**
     * Translate all token strings within the specified message using the
     * substitution services of the given translator.
     * 
     * @param message message string whose tokens are to be translated
     * @param translator token translator
     * @return translated message string
     */
    public String translate (String message, TokenTranslator translator);

}
