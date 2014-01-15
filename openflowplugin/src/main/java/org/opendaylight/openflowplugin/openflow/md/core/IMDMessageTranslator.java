/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;

/**
 * translates between messages
 * @param <IN> source message type
 * @param <OUT> result message type
 */
public interface IMDMessageTranslator<IN, OUT> {

    /**
     * This method is called in order to translate message to MD-SAL or from MD-SAL. 
     * 
     * @param cookie 
     *            auxiliary connection identifier 
     * @param sc
     *            The SessionContext which sent the OF message
     * @param msg
     *            The OF message
     *            
     * @return translated message
     */
    public OUT translate(SwitchConnectionDistinguisher cookie, SessionContext sc, IN msg);

}
