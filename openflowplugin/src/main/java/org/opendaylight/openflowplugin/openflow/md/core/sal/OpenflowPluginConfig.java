package org.opendaylight.openflowplugin.openflow.md.core.sal;

/**
 * Created by eshuvka on 5/16/2016.
 */
public class OpenflowPluginConfig {

    private final boolean m_tableFeaturesEnabled;

    private OpenflowPluginConfig (OpenflowPluginConfigBuilder builder){
        m_tableFeaturesEnabled = builder.isTableFeaturesEnabled();
    }

    public boolean isTableFeaturesEnabled(){
        return m_tableFeaturesEnabled;
    }

    public static OpenflowPluginConfigBuilder builder(){
        return new OpenflowPluginConfigBuilder();
    }

    public static class OpenflowPluginConfigBuilder{
        private boolean tableFeaturesEnabled;

        public boolean isTableFeaturesEnabled(){
            return tableFeaturesEnabled;
        }

        public void setTableFeatures(boolean isEnabled){
            tableFeaturesEnabled = isEnabled;
        }

        public OpenflowPluginConfig build() {return new OpenflowPluginConfig(this);}
    }
}
