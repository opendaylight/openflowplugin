/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

/**
 * @param <IN> rpc input value type
 * @param <OUT> rpc output value type
 */
public class RpcInputOutputTuple<IN, OUT> {
    
    private IN input;
    private OUT output;
    
    
    /**
     * @param input input
     * @param output output
     */
    public RpcInputOutputTuple(IN input, OUT output) {
        this.input = input;
        this.output = output;
    }
    
    /**
     * @return the input
     */
    public IN getInput() {
        return input;
    }
    /**
     * @return the output
     */
    public OUT getOutput() {
        return output;
    }

}
