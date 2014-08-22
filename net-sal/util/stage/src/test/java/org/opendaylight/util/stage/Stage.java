/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import static org.opendaylight.util.junit.TestTools.delay;
import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.opendaylight.util.stage.Data.A;
import org.opendaylight.util.stage.Data.B;
import org.opendaylight.util.stage.Data.C;
import org.opendaylight.util.stage.Data.D;

/**
 * Fixture specification for a process stage.
 *
 * @author Thomas Vachuska
 */

public abstract class Stage {

    public static interface One extends ProcessStageOutlet<Data.A, Data.B> {
    }

    public static interface Two extends ProcessStageOutlet<Data.B, Data.C> {
    }

    public static interface Three extends ProcessStageOutlet<Data.C, Void> {
    }

    public static interface Four extends ProcessStageOutlet<Data.B, Data.A> {
    }

    public static final int SIMULATED_PROCESSING_DELAY_MS = 50; 
    public static final int SIMULATED_IDLE_TIMEOUT_MS = 25; 
    
    // Test fixture process stage to exercise the base functionality
    public static abstract class Transmutor<T, P, Q> extends ExecutorDrivenProcessStage<T, P, Q> {
        
        List<T> items = new ArrayList<T>();
        List<P> results = new ArrayList<P>();
        List<T> discards = new ArrayList<T>();
        List<Q> branches = new ArrayList<Q>();
        
        int processingDelay = SIMULATED_PROCESSING_DELAY_MS;
        
        public Transmutor() { setIdleTimeOut(SIMULATED_IDLE_TIMEOUT_MS); }
        
        /**
         * @param item original item
         * @return true if the item should be discarded
         **/
        protected boolean shouldDiscard(T item) { 
            return item.toString().endsWith("!"); 
        }
        
        /**
         * @param item original item
         * @return true if the item should be branched
         **/
        protected boolean shouldBranch(T item) { return false; }
        
        /**
         * @param item original item
         * @return new alternate item to be branched
         **/
        protected Q createAlternate(T item) { return null;  }
        
        protected abstract P createResult(T item);

        @Override
        public P processItem(T item) {
            items.add(item);
            print("Transmuting " + item);
            
            // Optionally slow down the processing to facilitate easier
            // testing.
            if (processingDelay > 0)
                delay(processingDelay);

            // Attempt to create an alternate result and branch first.
            if (shouldBranch(item)) {
                Q alternate = createAlternate(item);
                print("Branching " + alternate);
                branches.add(alternate);
                branch(alternate);
                return null;
            }
            
            // Next, see if we should discard; if so, do it.
            if (shouldDiscard(item)) {
                print("Discarding " + item);
                discards.add(item);
                discard(item);
                return null;
            }
            
            // Otherwise, produce the result, record it and return it.
            P result = createResult(item);
            results.add(result);
            return result;
        }
        
        void verifyLists(int ei, int er, int ed, int eb) {
            assertEquals("incorrect item count", ei, items.size());
            assertEquals("incorrect result count", er, results.size());
            assertEquals("incorrect discard count", ed, discards.size());
            assertEquals("incorrect branch count", eb, branches.size());
        }

    }
    
    public static class FirstStage extends Transmutor<Data.A, Data.B, Void> {
        @Override
        protected B createResult(A item) {
            return item.toString().endsWith("!") ? null :
                    new B(item.toString().toLowerCase(Locale.getDefault()));
        }
    }
    
    public static class SecondStage extends Transmutor<Data.B, Data.C, Data.D> {
        @Override protected boolean shouldBranch(B item) {
            return item.toString().endsWith("?");
        }
        @Override protected D createAlternate(B item) {
            return new D(item.toString());
        }
        @Override protected C createResult(B item) { 
            return new C(item.toString().toUpperCase(Locale.getDefault()));
        }
    }
    
    public static class ThirdStage extends Transmutor<Data.C, Void, Void> {
        @Override protected Void createResult(C item) { 
            return null;
        }
    }
    
    public static class FourthStage extends Transmutor<Data.B, Data.A, Void> {
        @Override protected A createResult(B item) { 
            return new A(item.toString().toUpperCase(Locale.getDefault()));
        }
    }
    
}
