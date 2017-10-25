/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.statistics;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.openflowjava.protocol.spi.statistics.StatisticsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class to hold and process counters
 * @author madamjak
 *
 */
public final class StatisticsCounters implements StatisticsHandler {

    /**
     * Default delay between two writings into log (milliseconds)
     */
    public static final int DEFAULT_LOG_REPORT_PERIOD = 10000;
    /**
     * Minimal delay between two writings into log (milliseconds)
     */
    public static final int MINIMAL_LOG_REPORT_PERIOD = 500;
    private static StatisticsCounters instanceHolder;
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsCounters.class);

    private Timer logReporter;
    private int logReportPeriod;
    private boolean runLogReport;
    private Map<CounterEventTypes, Counter> countersMap;
    private boolean runCounting;
    // array to hold enabled counter types
    private CounterEventTypes[] enabledCounters = {
                    CounterEventTypes.DS_ENCODE_FAIL,
                    CounterEventTypes.DS_ENCODE_SUCCESS,
                    CounterEventTypes.DS_ENTERED_OFJAVA,
                    CounterEventTypes.DS_FLOW_MODS_ENTERED,
                    CounterEventTypes.DS_FLOW_MODS_SENT,
            CounterEventTypes.US_DROPPED_PACKET_IN,
                    CounterEventTypes.US_DECODE_FAIL,
                    CounterEventTypes.US_DECODE_SUCCESS,
                    CounterEventTypes.US_MESSAGE_PASS,
                    CounterEventTypes.US_RECEIVED_IN_OFJAVA};

    /**
     * Get instance of statistics counters, first created object does not start counting and log reporting
     * @return an instance
     */
    public static synchronized StatisticsCounters getInstance() {
        if (instanceHolder == null) {
            instanceHolder = new StatisticsCounters();
        }
        return instanceHolder;
    }

    private StatisticsCounters() {
        countersMap = new ConcurrentHashMap<>();
        for(CounterEventTypes cet : enabledCounters){
            countersMap.put(cet, new Counter());
        }
        runCounting = false;
        this.logReportPeriod = 0;
        this.runLogReport = false;
        LOG.debug("StaticsCounters has been created");
    }

    /**
     * Start counting (counters are set to 0 before counting starts)
     * @param reportToLogs - true = statistic counters will periodically log
     * @param logReportDelay - delay between two logs (in milliseconds)
     */
    public void startCounting(boolean reportToLogs, int logReportDelay){
        if (runCounting) {
            return;
        }
        resetCounters();
        LOG.debug("Counting started...");
        if(reportToLogs){
            startLogReport(logReportDelay);
        }
        runCounting = true;
    }

    /**
     * Stop counting, values in counters are untouched, log reporter is stopped
     */
    public void stopCounting(){
        runCounting = false;
        LOG.debug("Stop counting...");
        stopLogReport();
    }

    /**
     * Give an information if counting is running
     * @return true, if counting is running, otherwise false
     */
    public boolean isRunCounting(){
        return runCounting;
    }

    /**
     * Prints statistics with given delay between logs
     * @param logReportDelay - delay between two logs (in milliseconds)
     * @exception IllegalArgumentException if logReportDelay is less than 0
     */
    public void startLogReport(int logReportDelay){
        if(runLogReport){
            return;
        }
        if(logReportDelay <= 0){
            throw new IllegalArgumentException("logReportDelay has to be greater than 0");
        }
        if(logReportDelay < MINIMAL_LOG_REPORT_PERIOD){
            this.logReportPeriod = MINIMAL_LOG_REPORT_PERIOD;
        } else {
            this.logReportPeriod = logReportDelay;
        }
        logReporter = new Timer("SC_Timer");
        logReporter.schedule(new LogReporterTask(this), this.logReportPeriod, this.logReportPeriod);
        runLogReport = true;
        LOG.debug("Statistics log reporter has been scheduled with period {} ms", this.logReportPeriod);
    }

    /**
     * Stops logging, counting continues
     */
    public void stopLogReport(){
        if(runLogReport){
            if(logReporter != null){
                logReporter.cancel();
                LOG.debug("Statistics log reporter has been canceled");
            }
            runLogReport = false;
        }
    }

    /**
     * Give an information if log reporter is running (statistics are write into logs).
     * @return true if log reporter writes statistics into log, otherwise false
     */
    public boolean isRunLogReport(){
        return runLogReport;
    }

    /**
     * @return the current delay between two writings into logs
     */
    public int getLogReportPeriod() {
        return logReportPeriod;
    }

    /**
     * @return the enabled counters
     */
    protected CounterEventTypes[] getEnabledCounters() {
        return enabledCounters;
    }
    /**
     * @return the countersMap
     */
    protected Map<CounterEventTypes, Counter> getCountersMap() {
        return countersMap;
    }

    /**
     * Give an information if is given counter is enabled
     * @param counterEventKey
     * @return true if counter has been Enabled, otherwise false
     */
    public boolean isCounterEnabled(CounterEventTypes counterEventKey){
        if (counterEventKey == null) {
            return false;
        }
        return countersMap.containsKey(counterEventKey);
    }

    /**
     * Get counter by CounterEventType
     * @param counterEventKey key to identify counter (can not be null)
     * @return Counter object or null if counter has not been enabled
     * @throws IllegalArgumentException if counterEventKey is null
     */
    public Counter getCounter(CounterEventTypes counterEventKey) {
        if (counterEventKey == null) {
            throw new IllegalArgumentException("counterEventKey can not be null");
        }
        return countersMap.get(counterEventKey);
    }

    /**
     * Increment value of given counter
     * @param counterEventKey key to identify counter
     */
    public void incrementCounter(CounterEventTypes counterEventKey) {
        if(runCounting){
            if (isCounterEnabled(counterEventKey)){
                countersMap.get(counterEventKey).incrementCounter();
            }
        }
    }

    @Override
    public void resetCounters() {
        for(CounterEventTypes cet : enabledCounters){
            countersMap.get(cet).reset();
        }
        LOG.debug("StaticsCounters has been reset");
    }

    @Override
    public String printStatistics() {
        StringBuilder strBuilder = new StringBuilder();
        for(CounterEventTypes cet : getEnabledCounters()){
            strBuilder.append(cet.name() + ": " + getCountersMap().get(cet).getStat() + "\n");
        }
        return strBuilder.toString();
    }

    /**
     * internal class to process logReporter
     * @author madamjak
     *
     */
    private static class LogReporterTask extends TimerTask {
        private static final Logger LOG = LoggerFactory.getLogger(LogReporterTask.class);

        private StatisticsCounters sc;
        public LogReporterTask(StatisticsCounters sc) {
            this.sc = sc;
        }

        @Override
        public void run() {
            for(CounterEventTypes cet : sc.getEnabledCounters()){
                LOG.debug("{}: {}", cet.name(), sc.getCountersMap().get(cet).getStat());
            }
        }
    }
}
