/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.junit;

/**
 * Defines the API for a piece of code to be bench-marked by a
 * {@link BenchmarkRunner}.
 *
 * @author Simon Hunt
 */
public interface BenchmarkSubject {

    /**
     * Preparation method that makes sure the code under test is ready to run.
     */
    public void prepare();

    /**
     * Runs the code under test.
     * 
     * @throws Exception any unexpected exception 
     */
    public void run() throws Exception;
}
