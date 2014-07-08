/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin;

/**
 * Created by Martin Bobak mbobak@cisco.com on 7/8/14.
 */
public class ConnectionException extends Exception {

    public ConnectionException(String message){
        super(message);
    }

}
