/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.queue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;

/**
 * This processing mechanism based on queue. Processing consists of 2 steps: translate and publish.
 * Proposed workflow (might slightly deviate in implementations):
 * <ol>
 * <li>messages of input type are pushed in (via {@link QueueKeeper and similar})</li>
 * <li>ticket (executable task) is build upon each pushed message and enqueued</li>
 * <li>ticket is translated using appropriate translator</li>
 * <li>ticket is dequeued and result is published by appropriate popListener</li>
 * </ol>
 * Message order might be not important, e.g. when speed is of the essence
 * @param <I> source type (IN)
 * @param <O> result type (OUT)
 */
public interface QueueProcessor<I, O> extends MessageSourcePollRegistrator<QueueKeeper<I>>, Enqueuer<QueueItem<I>> {

    /**
     * translators for message processing.
     * @param translatorMapping translators for message processing
     */
    void setTranslatorMapping(Map<TranslatorKey, Collection<IMDMessageTranslator<I, List<O>>>> translatorMapping);

    /**
     * listeners invoked when processing done.
     * @param popListenersMapping listeners invoked when processing done
     */
    void setPopListenersMapping(Map<Class<? extends O>, Collection<PopListener<O>>> popListenersMapping);
}
