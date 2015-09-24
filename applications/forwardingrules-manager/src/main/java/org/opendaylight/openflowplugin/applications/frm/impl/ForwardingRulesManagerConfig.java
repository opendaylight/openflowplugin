package org.opendaylight.openflowplugin.applications.frm.impl;

/**
 * Created by efiijjp on 10/14/2015.
 */
public class ForwardingRulesManagerConfig {

    private final boolean staleMarkingEnabled;

    private ForwardingRulesManagerConfig(ForwardingRulesManagerConfigBuilder builder){
        this.staleMarkingEnabled = builder.isStaleMarkingEnabled();
    }

    public boolean isStaleMarkingEnabled(){
        return staleMarkingEnabled;
    }


    public static ForwardingRulesManagerConfigBuilder builder(){
        return new ForwardingRulesManagerConfigBuilder();
    }



    public static class ForwardingRulesManagerConfigBuilder {
        private boolean staleMarkingEnabled;

        public boolean isStaleMarkingEnabled(){
            return staleMarkingEnabled;
        }

        public void setStaleMarkingEnabled(boolean staleMarkingEnabledFlag){
            staleMarkingEnabled = staleMarkingEnabledFlag;
        }

        public ForwardingRulesManagerConfig build(){
            return new ForwardingRulesManagerConfig(this);
        }
    }

}
