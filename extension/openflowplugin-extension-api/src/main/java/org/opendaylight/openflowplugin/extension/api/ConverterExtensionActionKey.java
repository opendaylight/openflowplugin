/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;


/**
 * 
 */
public class ConverterExtensionActionKey extends ConverterExtensionKey<Action> {

    /**
     * @param type
     */
    public ConverterExtensionActionKey(Class<? extends Action> type) {
        super(type);
    }
    
}
