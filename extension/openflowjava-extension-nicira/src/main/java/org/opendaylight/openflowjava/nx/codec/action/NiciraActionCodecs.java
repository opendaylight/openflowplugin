/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

/**
 * @author msunal
 *
 */
public class NiciraActionCodecs {

    public static final RegMoveCodec REG_MOVE_CODEC = new RegMoveCodec();
    public static final RegLoadCodec REG_LOAD_CODEC = new RegLoadCodec();
    public static final OutputRegCodec OUTPUT_REG_CODEC = new OutputRegCodec();
}
