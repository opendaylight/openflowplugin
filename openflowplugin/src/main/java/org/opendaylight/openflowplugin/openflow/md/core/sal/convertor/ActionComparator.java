package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.Comparator;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;

public class ActionComparator implements Comparator<Action> {

    private static ActionComparator instance = new ActionComparator();
    public static ActionComparator toInstance() {
        return instance;
    }

    @Override
    public int compare(Action action1, Action action2) {
        if(action1 ==null || action2==null ) {
            throw new NullPointerException("Cannot compare null Actions");
        } else if (action1.getOrder() == null) {
            throw new NullPointerException(errorMsg(action1));
        } else if (action2.getOrder() == null) {
            throw new NullPointerException(errorMsg(action2));
        }
        return action1.getOrder().compareTo(action2.getOrder());
    }

    private String errorMsg(Action action) {
        return "Action " + action + "has getOrder() == null.  All actions must have an order.";
    }

}
