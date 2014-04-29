/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.queue;

/**
 * @author mkothand
 * 
 *
 */
public enum QueueKeeperType {
	
	/*
	 * Represents new connection before determining 
	 * if connection is Main or Auxiliary
	 */
	INITIAL,

    /*
	 * Represents Main connection
	 */
	MAIN,
	
    /*
	 * Represents Auxiliary connection
	 */
	AUX

}
