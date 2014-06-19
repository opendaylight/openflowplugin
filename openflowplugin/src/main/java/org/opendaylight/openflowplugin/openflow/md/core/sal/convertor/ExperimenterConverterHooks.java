/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.Map;
import java.util.HashMap;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ExperimenterConverter;

/**
 * Utility class for converting a MD-SAL Table features into the OF library table
 * features.
 */
public class ExperimenterConverterHooks<E extends DataObject> {
    private static final Logger logger = LoggerFactory.getLogger(ExperimenterConverterHooks.class);

    private static ExperimenterConverterHooks instance;
    private static Map<Class<?>, ExperimenterConverter> table;


    private ExperimenterConverterHooks() {
        // do nothing
    }

    /**
     * @return singleton instance
     */
    public static synchronized ExperimenterConverterHooks getInstance() {
        if (instance == null) {
            instance = new ExperimenterConverterHooks();
            instance.init();
        }
        return instance;
    }

    /**
     * Encoder table provisioning
     */
    public void init() {
        table = new HashMap<Class<?>, ExperimenterConverter>();        
    }
    
    public static void registerExperimenterConverter(Class<? extends DataObject> msgType, ExperimenterConverter msgTypeConverterInstance){
    	table.put(msgType, msgTypeConverterInstance);
    }
    

    /**
     * @param msgTypeKey
     * @return encoder for current type of message (msgTypeKey)
     */
    @SuppressWarnings("unchecked")
    public ExperimenterConverter getConverter(Class<E> msgTypeKey) {
        return (ExperimenterConverter) table.get(msgTypeKey);
    }


}