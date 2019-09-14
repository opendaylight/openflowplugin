/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsPollingServiceTest {
    @Mock
    private TimeCounter timeCounter;
    @Mock
    private Supplier<ListenableFuture<Boolean>> gatheringSupplier;

    private StatisticsPollingService statisticsPollingService;

    @Before
    public void setUp() {
        when(timeCounter.getAverageTimeBetweenMarks()).thenReturn(15000L);
        when(gatheringSupplier.get()).thenReturn(Futures.immediateFuture(true));
        statisticsPollingService = new StatisticsPollingService(
                timeCounter, 10000, 12000,
                gatheringSupplier);
    }

    @Test
    public void startUp() {
        statisticsPollingService.startUp();
        verify(timeCounter).markStart();
    }

    @Test
    public void runOneIteration() throws Exception {
        statisticsPollingService.runOneIteration();
        verify(gatheringSupplier).get();
        verify(timeCounter).addTimeMark();
    }

    @Test
    public void scheduler() {
        statisticsPollingService.scheduler();
        verify(timeCounter).getAverageTimeBetweenMarks();
    }

}