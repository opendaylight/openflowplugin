/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.Date;

/**
 * Simple facade for obtaining current time and date. The clock source can be
 * the default {@link java.lang.System#currentTimeMillis()} or for testing
 * purposes, it can be an internal source whose time can be manipulated.
 * <p>
 * Use of this facade allows applications sensitive to time-related context to
 * be easily unit tested under simulated and accelerated time-lines.
 * 
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public final class Clock {

    private static WarpingTimeSource source = null;
    
    private static final WarpingTimeSource SYSTEM_TIME = new SystemTimeSource();
    private static final WarpingTimeSource WARPED_TIME = new TestTimeSource();
    
    // no instantiation -- we have one time source associated with the class itself
    private Clock() { }

    /**
     * Sets the time-source to one backed by 
     * {@link java.lang.System#currentTimeMillis()} and asserts that no other
     * time-source has been previously set.
     * <p>
     * Calling this method multiple times has no side effects.
     * 
     *  @throws IllegalStateException if a time-source, other than has been 
     *  previously latched
     */
    public static synchronized void assertRealTimeSource() 
                                        throws IllegalStateException {
        if (source == null)
            source = SYSTEM_TIME;
        else if (source != SYSTEM_TIME)
            throw new IllegalStateException("Another time-source has already been activated");
    }

    /**
     * Sets the time-source to one capable of being warped relative to the
     * {@link java.lang.System#currentTimeMillis()} and asserts that no other
     * time-source has been previously set.
     * <p>
     * Calling this method multiple times has no side effects.
     * 
     *  @throws IllegalStateException if a time-source, other than this one
     *  has been previously latches
     */
    public static synchronized void assertTestTimeSource() {
        if (source == null)
            source = WARPED_TIME;
        else if (source != WARPED_TIME)
            throw new IllegalStateException("Another time-source has already been activated");
    }
    
    /**
     * Get the current time source, which may be capable of time-warping
     * 
     * @return current time source
     */
    public static WarpingTimeSource source() {
        return source;
    }
    
    /**
     * Get the current time in milliseconds since start of epoch.
     * 
     * @return number of milliseconds elapsed since start of epoch
     * @see java.lang.System#currentTimeMillis()
     */
    public static long currentTimeMillis() {
        return source.currentTimeMillis();
    }
    
    /**
     * Get the date corresponding to the current time of the source.
     * 
     * @return number of milliseconds elapsed since start of epoch
     * @see java.lang.System#currentTimeMillis()
     * @see java.util.Date#Date(long)
     */
    public static Date date() {
        return new Date(source.currentTimeMillis());
    }
    


    /**
     * Facade for implementing a time source capable of being warped with respect to real time.
     */
    public interface WarpingTimeSource {
        
        /**
         * Get the current time in milliseconds since start of epoch.
         * 
         * @return number of milliseconds elapsed since start of epoch
         * @see java.lang.System#currentTimeMillis()
         */
        public long currentTimeMillis();
        
        /**
         * Predicate that allows callers to determine whether the time-source
         * supports warping operations.
         * 
         * @return true if time warping is supported; false otherwise
         */
        public boolean supportsWarping();
        
        /**
         * Set the current time in milliseconds since start of epoch. This
         * number will be recorded as a current offset and any time elapsed
         * from this time will be considered relative to this offset.
         * 
         * @param ms number of milliseconds elapsed since start of epoch that comprises the current time
         * @see java.lang.System#currentTimeMillis()
         * @throws UnsupportedOperationException thrown if the source does
         *         not support time warping
         */
        public void setCurrentTimeMillis(long ms);

        /**
         * Set the milliseconds of time offset relative to current time.
         * 
         * @param ms number of milliseconds of offset from the current time
         * @throws UnsupportedOperationException thrown if the source does
         *         not support time warping
         */
        public void setTimeOffset(long ms);

    }
    
    /**
     * Implementation of a time-source backed by the system clock.
     */
    private static class SystemTimeSource implements WarpingTimeSource {

        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
        
        @Override
        public boolean supportsWarping() {
            return false;
        }

        @Override
        public void setCurrentTimeMillis(long ms) {
            throw new UnsupportedOperationException("Warping not supported for system-backed time source.");
        }
        
        @Override
        public void setTimeOffset(long ms) {
            throw new UnsupportedOperationException("Warping not supported for system-backed time source.");
        }
        
    }

    
    /**
     * Implementation of a time-source capable of being warped relative to the
     * system clock.
     */
    private static class TestTimeSource implements WarpingTimeSource {
        
        private long offset = 0;

        @Override
        public synchronized long currentTimeMillis() {
            return offset + System.currentTimeMillis();
        }

        @Override
        public boolean supportsWarping() {
            return true;
        }
        
        @Override
        public void setCurrentTimeMillis(long ms) {
            setTimeOffset(ms - System.currentTimeMillis());
        }

        @Override
        public synchronized void setTimeOffset(long ms) {
            this.offset = ms;
        }
        
    }
    
    /**
     * Resets the current source. This is provided purely to allow this facade
     * to be unit tested with different time-sources in a single life-time,
     * which is something that is not otherwise allowed due to the latching
     * nature of applying a time-source.
     */
    static synchronized void resetSourceUnderPenaltyOfDeath() {
        source = null;
    }

}
