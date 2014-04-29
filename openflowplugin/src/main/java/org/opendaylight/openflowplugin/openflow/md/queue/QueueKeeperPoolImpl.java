/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author mkothand
 *
 *
 */

public class QueueKeeperPoolImpl implements QueueKeeperPool {

    private static final Logger LOG = LoggerFactory
            .getLogger(QueueKeeperPoolImpl.class);


	private QueueKeeper<OfHeader,DataObject> initialConnectionQueueKeeper;
	private final List<QueueKeeper<OfHeader,DataObject>> queueKeeperMainConnectionPool = new ArrayList<QueueKeeper<OfHeader,DataObject>>();
	private final List<QueueKeeper<OfHeader,DataObject>> queueKeeperAuxConnectionPool = new ArrayList<QueueKeeper<OfHeader,DataObject>>();

	private ScheduledThreadPoolExecutor pool;
	private MessageSpy<OfHeader, DataObject> messageSpy;

	private static int mainConnQkPoolSize = 5;
	private static int auxConnQkPoolSize = 5;
	private static int qkExecutorPoolSize = 10;


	public QueueKeeperPoolImpl(){
	}

	@Override
	public void initialize(MessageSpy<OfHeader, DataObject> messageSpy) {
		// read from config subsystem and initialize the qkPoolSize and qkExecutorPoolSize
		readAndSetConfig();

		// Statically initialize the common thread pool and qk-pool
		this.messageSpy = messageSpy;
		this.pool = new ScheduledThreadPoolExecutor(qkExecutorPoolSize);
		if (mainConnQkPoolSize > 0 && auxConnQkPoolSize > 0){
		    prepareInitialConnectionQueueKeeper();
			prepareMainConnectionQKPool();
			prepareAuxConnectionQKPool();
			LOG.info("QueueKeeper Pool initialized with dedicated pools for Main and Aux connections");
			LOG.debug("Main Connection Pool size : {}", mainConnQkPoolSize);
			LOG.debug("Auxiliary Connection Pool size : {}", auxConnQkPoolSize);
			LOG.debug("Initial QK instance created");
		}
		else{
			prepareInitialConnectionQueueKeeper();
			LOG.info("QueueKeeper Pool initialized with single default keeper instance");

		}

	}

	@Override
	public QueueKeeper<OfHeader,DataObject> selectQueueKeeper(int connectionId,
			QueueKeeperType qkType ) {
		// since connectionId is a monotonously increasing number for all connections across the
		// switches

	    if (qkType == QueueKeeperType.INITIAL){
			LOG.debug("Selecting Common QueueKeeper instance for Initial-Connection");

	    	return selectQKForInitialConnection();
	    }

	    if (mainConnQkPoolSize > 0 && auxConnQkPoolSize > 0){
	       if (qkType == QueueKeeperType.MAIN){
				LOG.debug("Selecting Main-Connection QueueKeeper instance from pool for ConnectionId : {}", connectionId);
	    	   return selectQKForMainConnection(connectionId);
	       }
	       else{
				LOG.debug("Selecting Auxiliary-Connection QueueKeeper instance from pool for ConnectionId : {}", connectionId);
	    	   return selectQKForAuxiliaryConnection(connectionId);
	       }
	    }
	    else{
	    	// Always select single common queuekeeper
	    	// TODO: Actually, caller need not re-assign or switch connection in this
	    	// case. can we just return null so that caller is signalled to
	    	// retain existing QueueKeeper assigned during initialization of
	    	// ConnectionConductorImpl. Looks nasty but can avoid unnecessary
	    	// reassignment at caller's end
			LOG.debug("Selecting Common QueueKeeper instance post-handshake");
	    	return selectQKForInitialConnection();
	    }


	}

	private QueueKeeper<OfHeader,DataObject> createQueueKeeper(MessageSpy<OfHeader, DataObject> messageSpy){
		QueueKeeperLightImpl qKeeper = new QueueKeeperLightImpl();
        qKeeper.setTranslatorMapping(OFSessionUtil.getTranslatorMap());
        qKeeper.setPopListenersMapping(OFSessionUtil.getPopListenerMapping());
        qKeeper.setMessageSpy(messageSpy);
        qKeeper.init();

        // This is added mainly to avoid every QueueKeeper instance from
        // creating its own thread pool. Instead inject a common thread-pool to all
        // QueueKeeper instances maintained by this pool
        qKeeper.setThreadPool(pool);
        return qKeeper;


	}

	private void prepareInitialConnectionQueueKeeper(){

		this.initialConnectionQueueKeeper = createQueueKeeper(messageSpy);
		LOG.info("Initial QueueKeeper instance {} created");
	}

	private void prepareMainConnectionQKPool(){
		for (int i=0; i< mainConnQkPoolSize; i++){
			QueueKeeper<OfHeader,DataObject> qk = createQueueKeeper(messageSpy);
			queueKeeperMainConnectionPool.add(qk);
		}
		LOG.info("Main-Connection QueueKeeper pool populated");
	}

	private void prepareAuxConnectionQKPool(){
		for (int i=0; i< auxConnQkPoolSize; i++){
			QueueKeeper<OfHeader,DataObject> qk = createQueueKeeper(messageSpy);
			queueKeeperAuxConnectionPool.add(createQueueKeeper(messageSpy));
		}
		LOG.info("Auxiliary-Connection QueueKeeper pool populated");
	}

	/*
	 * This QueueKeeper is a single fixed instance usable for
	 */
	private QueueKeeper<OfHeader,DataObject> selectQKForInitialConnection(){
		return initialConnectionQueueKeeper;
	}

	/*
	 * Uses simple modulo based round-robin selection
	 * This would ensure that selection is round-robin as long as
	 * connectionId increases monotonously
	 *
	 */
	private QueueKeeper<OfHeader,DataObject> selectQKForMainConnection(int connectionId){
		return queueKeeperMainConnectionPool.get(connectionId % mainConnQkPoolSize) ;
	}

	/*
	 * Uses simple modulo based round-robin selection
	 * This would ensure that selection is round-robin as long as
	 * connectionId increases monotonously
	 *
	 */
	private QueueKeeper<OfHeader,DataObject> selectQKForAuxiliaryConnection(int connectionId){
		return queueKeeperAuxConnectionPool.get(connectionId % auxConnQkPoolSize) ;
	}

	private void readAndSetConfig(){
		// TODO: read from configuration following params and set the values to override
		// static defaults
		// 1. mainConnQkPoolSize
		// 2. auxConnQkPoolSize
		// 3. qkExecutorPoolSize

	}

	@Override
	public boolean isExtendedPool() {
		if (mainConnQkPoolSize == 0 && auxConnQkPoolSize == 0){
			return false;
		}
		return true;
	}




}
