package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.Ordered;

import java.util.Comparator;

/**
 * Comparator for comparing objects which extend Ordered.
 *
 * @param <T>
 */
public class OrderComparator<T extends Ordered> implements Comparator<T> {

    private static OrderComparator instance = new OrderComparator();
    public static OrderComparator toInstance() {
        return instance;
    }

    @Override
    public int compare(T obj1, T obj2) {
        if(obj1 ==null || obj2==null ) {
            throw new NullPointerException("Cannot compare null Actions");
        } else if (obj1.getOrder() == null) {
            throw new NullPointerException(errorMsg(obj1));
        } else if (obj2.getOrder() == null) {
            throw new NullPointerException(errorMsg(obj2));
        }
        return obj1.getOrder().compareTo(obj2.getOrder());
    }

    private String errorMsg(T obj) {
        return "The comparing model " + obj + "has getOrder() == null. An order is mandatory";
    }
}
