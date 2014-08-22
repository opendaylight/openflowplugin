/*
 * (C) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Executor.
 * 
 * @param <T> type of the execution result
 * @param <I> type of the execution input
 * @see Instruction
 * @see Procedure
 * @author Fabiel Zuniga
 */
public interface Executor<T, I> {

    /**
     * Executes the instructions.
     * 
     * @param input input
     * @return execution result
     */
    public T execute(I input);
}
