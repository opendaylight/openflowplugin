/**
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

/**
 * Created by efiijjp on 10/14/2015.
 */
public class ForwardingRulesManagerConfig {

    private final boolean m_staleMarkingEnabled;
    private final int m_reconciliationRetryCount;

    private ForwardingRulesManagerConfig(ForwardingRulesManagerConfigBuilder builder){
        m_staleMarkingEnabled = builder.isStaleMarkingEnabled();
        m_reconciliationRetryCount = builder.getReconciliationRetryCount();
    }

    public boolean isStaleMarkingEnabled(){
        return m_staleMarkingEnabled;
    }

    public int getReconciliationRetryCount() {
        return m_reconciliationRetryCount;
    }


    public static ForwardingRulesManagerConfigBuilder builder(){
        return new ForwardingRulesManagerConfigBuilder();
    }



    public static class ForwardingRulesManagerConfigBuilder {
        private boolean staleMarkingEnabled ;
        private int reconciliationRetryCount ;

        public boolean isStaleMarkingEnabled(){
            return staleMarkingEnabled;
        }
        public int getReconciliationRetryCount() {return reconciliationRetryCount;}

        public void setStaleMarkingEnabled(boolean staleMarkingEnabledFlag){
            staleMarkingEnabled = staleMarkingEnabledFlag;
        }

        public void setReconciliationRetryCount(int retryCount ){
            reconciliationRetryCount = retryCount;
        }

        public ForwardingRulesManagerConfig build(){
            return new ForwardingRulesManagerConfig(this);
        }
    }

}
