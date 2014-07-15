package org.opendaylight.openflowplugin.test.ForwardingConsumer;


/**
 * Created by brent on 7/11/14.
 */
public interface FlowClientOLD {

    public void  buildMatch();

    public void  buildInstruction();

    public void  buildFlowParameters();
    
    public void  builddFlow();

}
