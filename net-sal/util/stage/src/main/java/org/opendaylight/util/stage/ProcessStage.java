/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

/**
 * Abstraction of a process stage with a discrete start, stop and that
 * supports predicates for notions being stopped, finished and/or idle.
 * <p>
 * Implementations are expected to support ability to be run (started and
 * stopped) repeatedly, and without transferring any state from run to run.
 * 
 * @author Thomas Vachuska
 */
public interface ProcessStage {

    /**
     * Start the process stage.
     */
    public void start();

    /**
     * Stop the process stage.
     */
    public void stop();

    /**
     * Forcefully stop the process stage.
     */
    public void forceStop();

    /**
     * Indicates whether the process stage has been stopped.
     * 
     * @return true if the stage has been stopped; false otherwise.
     */
    public boolean isStopped();

    /**
     * Indicates whether the process stage is finished. A stage can be
     * considered finished only after it has been stopped.
     * 
     * @return true if the stage is stopped and finished processing; false
     *         otherwise
     */
    public boolean isFinished();

    /**
     * Indicates whether the process stage is to be considered idle. If the
     * stage is finished, it must also be idle.
     * 
     * @return true if the process stage is idle; false otherwise
     */
    public boolean isIdle();

}
