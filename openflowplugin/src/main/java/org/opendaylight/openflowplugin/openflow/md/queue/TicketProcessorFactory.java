/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @param <IN> type of DataObject
 * @param <OUT> type of DataObject
 */
public interface TicketProcessorFactory<IN extends DataObject, OUT extends DataObject> {

    /**
     * @param ticket ticket
     * @return runnable ticket processor
     */
    Runnable createProcessor(final Ticket<IN, OUT> ticket);

    /**
     * @param ticket ticket
     * @return runnable ticket processor
     */
    Runnable createSyncProcessor(final Ticket<IN, OUT> ticket);

    /**
     * @param ticket ticket
     * @return translated messages
     * 
     */
    List<OUT> translate(Ticket<IN, OUT> ticket);

    /**
     * @param ticketFinisher setter
     */
    void setTicketFinisher(TicketFinisher<OUT> ticketFinisher);

    /**
     * @param spy setter
     */
    void setSpy(MessageSpy<DataContainer> spy);

    /**
     * @param translatorMapping setter
     */
    void setTranslatorMapping(Map<TranslatorKey, Collection<IMDMessageTranslator<IN, List<OUT>>>> translatorMapping);
}
