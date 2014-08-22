/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Restrict-able object.
 * <p>
 * This class is used to assist a pattern that gives the creator of an object
 * more rights.
 * <p>
 * For example, assume we have a service that offers some business logic, but
 * the service can also be started and stopped for example). We don't want to
 * expose start() and stop() to all consumers of the service. Those methods
 * should be visible (or allowed) just for the component in charge of managing
 * the services (or the creator of the service).
 * <p>
 * {@code Restrictable} along with a factory method could be used to restrict
 * access to privileged functionality.
 * <p>
 * <strong>The usage of this class has some advantages. Avoids mixing the API
 * for two groups of people: privileged and regular users. Privileged users
 * use a different API to perform restricted operations. It also avoids a
 * state-based API where the same sequence of method calls might work or might
 * throw an exception, depending on whether the consumer is a privileged user
 * or a regular one. </strong>
 * <p>
 * Example:
 * 
 * <pre>
 * public interface PublicService {
 *     public void consumeService();
 * }
 * 
 * public interface RestrictedService {
 *     public void star();
 *     public void stop();
 * }
 * 
 * // Note: Configuration is not relevant for this example, however it also provides 
 * // a way of configuring the object by the creator (Give more rights to the creator to 
 * // configure the object). Once the object is configured it cannot be changed.
 * public class Configuration {
 *     // This configuration is specific for the implementation. 
 *     // Different implementations may be configured differently.
 *     // Make setters public.
 *     // Make getters package private if possible.
 * }
 * 
 * public final class ServiceImplementation implements PublicService {
 * 
 *     private Configuration configuration;
 *     
 *     // Must be final since it will be accessible by the creator and the reference cannot longer be retrieved (refreshed) after creation.
 *     private final RestrictedService restrictedApi;
 * 
 *     private ServiceImplementation(Configuration configuration) {
 *         if (configuration == null) {
 *             throw new NullPointerException(&quot;configuration cannot be null&quot;);
 *         }
 *         this.configuration = configuration;
 *         this.restrictedApi = new RestrictedApiImpl();
 *     }
 * 
 *     public static Restrictable&lt;MyPublicService, RestrictedService&gt; create(Configuration configuration) {
 *         ServiceImplementation restrictable = new ServiceImplementation(configuration);
 *         return Restrictable.create((MyPublicService)restrictable, restrictable.restrictedApi);
 *     }
 * 
 *     {@literal @}Override
 *     public void consumeService() {
 *         // ...
 *     }
 * 
 *     private void doSart(){
 *         // ...
 *     }
 * 
 *     private void doStop(){
 *         // ...
 *     }
 * 
 *     private class RestrictedApiImpl implements RestrictedService {
 *         // This class is non-static so it uses the same state of ServiceImplementation.
 *         // It just implements the restricted part.
 *         // This implementation could just call private methods in ServiceImplementation.
 * 
 *         {@literal @}Override
 *         public void start() {
 *             doSart();
 *         }
 * 
 *         {@literal @}Override
 *         public void stop() {
 *             doStop();
 *         }
 *     }
 * }
 * </pre>
 * 
 * <hr>
 * A restrictable object doesn't have to implement an interface to use this
 * pattern; the object itself may represent the public part. Example:
 * 
 * <pre>
 * 
 * public interface RestrictedApi {
 *     public void restrictedMethod();
 * }
 * 
 * public final class MyRestrictable {
 * 
 *     // Must be final since it will be accessible by the creator and the reference cannot longer be retrieved (refreshed) after creation.
 *     private final RestrictedApi restrictedApi = new RestrictedApi() {
 *         {@literal @}Override
 *         public void restrictedMethod() {
 *             doRestrictedMethod();
 *         }
 *     };
 * 
 *     private MyRestrictable() {
 *     
 *     }
 * 
 *     public static Restrictable&lt;MyRestrictable, RestrictedApi&gt; create() {
 *         MyRestrictable restrictable = new MyRestrictable();
 *         return Restrictable.create(restrictable, restrictable.restrictedApi);
 *     }
 * 
 *     private void doRestrictedMethod(){
 *         // ...
 *     }
 *     
 *     // ...
 * }
 * </pre>
 * 
 * @param <P> type of the public part of the object
 * @param <R> type of the restricted part of the object
 * @author Fabiel Zuniga
 */
public final class Restrictable<P, R> {

    private final P publicPart;
    private final R restrictedPart;

    private Restrictable(P publicPart, R restrictedPart) {
        if (publicPart == null) {
            throw new NullPointerException("publicPart cannot be null");
        }

        if (restrictedPart == null) {
            throw new NullPointerException("restrictedPart cannot be null");
        }

        this.publicPart = publicPart;
        this.restrictedPart = restrictedPart;
    }

    /**
     * Creates a restrict-able object.
     * 
     * @param publicPart object's public part
     * @param restrictedPart objects's restricted part
     * @return a restrict-able object
     */
    public static <P, R> Restrictable<P, R> create(P publicPart,
                                                   R restrictedPart) {
        return new Restrictable<P, R>(publicPart, restrictedPart);
    }

    /**
     * Gets the public part of the object.
     * 
     * @return the public part
     */
    public P getPublic() {
        return this.publicPart;
    }

    /**
     * Gets the restricted part of the object.
     * 
     * @return the restricted part
     */
    public R getRestricted() {
        return this.restrictedPart;
    }
}
