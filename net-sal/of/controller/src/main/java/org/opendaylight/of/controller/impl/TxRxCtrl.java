/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.TxRxControl;
import org.opendaylight.of.controller.TxRxTrigger;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.NotYetImplementedException;
import org.opendaylight.util.ResourceUtils;

import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.opendaylight.of.controller.CheckpointEvent.Code.RECORDING_STARTED;
import static org.opendaylight.of.controller.CheckpointEvent.Code.RECORDING_STOPPED;
import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;

/**
 * Implementation of TxRxControl - Diagnostics available via the TxRxQueue
 * which records all traffic through the controller.
 *
 * @see TxRxControl
 * @author Simon Hunt
 */
class TxRxCtrl implements TxRxControl {

    private static final String E_ALREADY_RECORDING = "Already recording";
    private static final String E_SECS_TOO_SMALL = "Seconds must be > 0";

    private static final ResourceBundle RES =
            ResourceUtils.getBundledResource(TxRxCtrl.class);
    private static final String MSG_REC_STARTED = RES.getString("recStarted");
    private static final String MSG_REC_STOPPED_TIMER =
            RES.getString("recStoppedTimer");
    private static final String MSG_REC_STOPPED_FORCED =
            RES.getString("recStoppedForced");

    /** The encapsulated queue. */
    private final BlockingQueue<MessageEvent> queue;

    private final Object lock = new Object();
    volatile boolean recording = false;

    private final ScheduledExecutorService timerService =
        newSingleThreadScheduledExecutor(namedThreads("TxRxShutoff"));
    private ScheduledFuture<?> timerFuture;

//    private TxRxTrigger trigger;
    private boolean waitingToFireTrigger;
    private String shutOffStr;
    private long startTimeMs;


    /** Constructs our queue consumer.
     *
     * @param queue the wrapped queue
     */
    TxRxCtrl(BlockingQueue<MessageEvent> queue) {
        this.queue = queue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{TxRxCtrl:");
        if (recording) {
            long elapsed = System.currentTimeMillis() - startTimeMs;
            sb.append("[<<REC:").append(shutOffStr).append(">> elapsed=")
                    .append(elapsed).append("ms]");
        } else if (waitingToFireTrigger) {
            sb.append("[<<TRIG-WAIT:").append(shutOffStr).append(">>");
        } else {
            sb.append("[<<STOPPED>>]");
        }
        sb.append(",Qsize=").append(queue.size()).append("}");
        return sb.toString();
    }

    /**
     * Returns a multi-line string representation of the queue control,
     * which includes the list of queued messages. Note that this is a
     * non-destructive read of the queue contents by taking a snapshot
     * of the queue.
     *
     * @return a multi-line string representation of the queue control.
     */
    public String toDebugString() {
        return makeDebugString(false);
    }

    /**
     * Returns a multi-line string representation of the queue control,
     * which includes the list of queued messages. Note that this is a
     * non-destructive read of the queue contents by taking a snapshot
     * of the queue. This particular method will include the detailed
     * debug string representations of each message, within each event.
     *
     * @return a multi-line string representation of the queue control.
     */
    public String toDetailedDebugString() {
        return makeDebugString(true);
    }

    private String makeDebugString(boolean detailed) {
        StringBuilder sb = new StringBuilder(toString());
        MessageEvent[] queuedMsgs =
                queue.toArray(new MessageEvent[queue.size()]);
        for (MessageEvent me: queuedMsgs) {
            sb.append(EOLI).append(me);
            if (detailed) {
                OpenflowMessage ofm = me.msg();
                if (ofm == null)
                    sb.append(EOLI).append("  (message is null)");
                else
                    sb.append(EOLI).append(ofm.toDebugString()).append(EOLI);
            }
        }
        sb.append(EOLI).append("---");
        return sb.toString();
    }

    /**
     * Returns an array containing the message events currently in the
     * queue. Note that this is a non-destructive read of the queue.
     * 
     * @return an array of message events in the queue
     */
    public MessageEvent[] getEvents() {
        return queue.toArray(new MessageEvent[queue.size()]);
    } 

    private void injectRecordingStartedCheckpoint(String shutOffStr) {
        String msg = MSG_REC_STARTED + " [" + shutOffStr + "]";
        queue.add(new CheckpointEvt(RECORDING_STARTED, msg));
    }

    private void injectRecordingStoppedCheckpoint(String text) {
        queue.add(new CheckpointEvt(RECORDING_STOPPED, text));
    }

    @Override
    public void startRecording(int seconds) {
        if (seconds < 1)
            throw new IllegalArgumentException(E_SECS_TOO_SMALL);
        long ms = (long)seconds * 1000;
        startRecording(ms);
    }

    /** Package private method for use by unit tests that do not want
     * to wait around for the minimum of 1 second for auto-recording
     * to shut off.
     *
     * @param millis the number of milliseconds before shutoff
     */
    void startRecording(long millis) {
        synchronized (lock) {
            if (recording)
                throw new IllegalStateException(E_ALREADY_RECORDING);
            queue.clear();
            long seconds = millis / 1000;
            shutOffStr = millis % 1000 != 0 ? millis + "ms" : seconds + "s";
            injectRecordingStartedCheckpoint(shutOffStr);
            startTimeMs = System.currentTimeMillis();
            timerFuture = timerService.schedule(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        recording = false;
                        injectRecordingStoppedCheckpoint(MSG_REC_STOPPED_TIMER);
                    } // sync
                }
            }, millis, TimeUnit.MILLISECONDS);
            recording = true;
        } // sync
    }


    @Override
    public void triggerRecording(TxRxTrigger trigger, int seconds) {
        // TODO : triggered recording
        throw new NotYetImplementedException("Sorry!");
    }

    @Override
    public void stopRecording() {
        synchronized (lock) {
            if (recording) {
                recording = false;
                injectRecordingStoppedCheckpoint(MSG_REC_STOPPED_FORCED);
            }
            if (timerFuture != null)
                timerFuture.cancel(true);
        } // sync
    }

    @Override
    public boolean isRecording() {
        synchronized (lock) {
            return recording;
        } // sync
    }

    @Override
    public boolean isTriggerWaiting() {
        synchronized (lock) {
            // TODO: implement trigger waiting
            return false;
        } // sync
    }

    @Override
    public MessageEvent take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public MessageEvent poll(long timeoutMs) throws InterruptedException {
        return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
