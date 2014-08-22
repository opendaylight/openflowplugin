/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.Assert;

/**
 * Tester to test objects which implement Serializable.
 * 
 * @author Fabiel Zuniga
 */
public final class SerializabilityTester {

    private SerializabilityTester() {

    }

    /**
     * Test serialization's binary compatibility. The preferred method should
     * be
     * {@link #testSerialization(Serializable, SemanticCompatibilityVerifier)}
     * 
     * @param <T> Type of the serializable entity.
     * @param entity Entity to serialize.
     */
    public static <T extends Serializable> void testSerialization(T entity) {
        testSerialization(entity, null);
    }

    /**
     * Test serialization's binary and semantic compatibility.
     * 
     * @param <T> type of the serializable entity
     * @param serializable serializable object
     * @param semanticCompatibilityVerifier semantic compatibility verifier to
     *        assert that {@code original} and its replica are semantically
     *        compatible. If {@code null} is provided just binary
     *        compatibility will be tested (The only errors that will be
     *        caught via this test is situations where an non-serializable
     *        object reference with a non-null value is used -
     *        {@link NotSerializableException}). Thus it is highly recommended
     *        to pass a non-null {@code semanticCompatibilityVerifier}.
     */
    public static <T extends Serializable> void testSerialization(T serializable,
            SemanticCompatibilityVerifier<T> semanticCompatibilityVerifier) {
        byte[] serialization = null;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(serializable);
            serialization = byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Assert.fail("Serialization failure: " + e.toString());
        }

        Object replicaObj = null;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serialization);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            replicaObj = objectInputStream.readObject();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Assert.fail("Deserialization failure: " + e.toString());
        }

        if (semanticCompatibilityVerifier != null) {
            @SuppressWarnings("unchecked")
            T replica = (T) replicaObj;
            semanticCompatibilityVerifier.assertSemanticCompatibility(serializable, replica);
        }
    }

    /**
     * Tests portable serialization between a different version of a class and
     * the current version.
     * <p>
     * Both binary and semantic compatibility are tested.
     * <p>
     * The current version is part of the class path and thus loaded using the
     * default class loader.
     * <p>
     * Process:
     * 
     * <pre>
     * Let ClassA and ClassA' be two different versions of the same class
     * Let objA be an instance of ClassA
     * 1. Serialize objA into the stream of bytes bA</li>
     * 2. Deserialize bA using ClassA' producing instance objA'</li>
     * 3. Serialize objA' into the stream of bytes bA'</li>
     * 4. Deserialize bA' using ClassA producing instance objA_2</li>
     * 5. Assert that objA (original) and objA_2 (replica) are semantically compatible (Not necessarily equals) </li>
     * </pre>
     * 
     * @param <T> type of the serializable entity
     * @param portableClass portable class
     * @param original current version instance of the portable class
     * @param differentVersionPath path of the jar file containing a different
     *        version of {@code portableClass}
     * @param semanticCompatibilityVerifier semantic compatibility verifier to
     *        assert that {@code original} and its replica reconstructed from
     *        a different class version are semantically compatible
     * @throws Exception if errors happen while testing portable serialization
     */
    public static <T extends Serializable> void testPortableSerialization(Class<T> portableClass, T original,
            Path differentVersionPath, SemanticCompatibilityVerifier<T> semanticCompatibilityVerifier) throws Exception {
        if (portableClass == null) {
            throw new NullPointerException("portableClass cannot be null");
        }

        if (original == null) {
            throw new NullPointerException("original cannot be null");
        }

        if (differentVersionPath == null) {
            throw new NullPointerException("differentVersionPath cannot be null");
        }

        ClassLoader differentVersionLoader = new ClassReloader(differentVersionPath, Thread.currentThread()
                .getContextClassLoader(), portableClass.getName());

        Class<?> differentVersionClass = differentVersionLoader.loadClass(portableClass.getName());

        Assert.assertFalse(portableClass.equals(differentVersionClass));

        Serializable differentVersionInstance = null;
        Serializable portableReplica = null;

        byte[] serialization = null;

        // Serializes original

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(original);
            serialization = byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Serialization of original failure: " + e.toString());
        }

        // Deserializes original using differentVersionLoader to produce a
        // different version instance

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serialization);
                ObjectInputStream objectInputStream = new ClassLoaderObjectInputStream(byteArrayInputStream,
                        differentVersionLoader)) {
            differentVersionInstance = (Serializable) objectInputStream.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Deserialization of original in different version failure: " + e.toString());
        }

        // Serializes different version instance

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(differentVersionInstance);
            serialization = byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Assert.fail("Serialization of different version failure: " + e.toString());
        }

        // Deserializes different version bytes using original's classloader
        // to produce a compatible version replica

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serialization);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            portableReplica = (Serializable) objectInputStream.readObject();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Assert.fail("Deserialization of different version in original (replica) failure: " + e.toString());
        }

        /*
        System.out.println("Original: " + original);
        System.out.println("Different Version Instance: " + differentVersionInstance);
        System.out.println("Portable Replica: " + portableReplica);
        */

        if (semanticCompatibilityVerifier != null) {
            @SuppressWarnings("unchecked")
            T replica = (T) portableReplica;
            semanticCompatibilityVerifier.assertSemanticCompatibility(original, replica);
        }
    }

    /**
     * Semantic compatibility verifier.
     *
     * @param <T> type of the serializable class
     */
    public static interface SemanticCompatibilityVerifier<T extends Serializable> {

        /**
         * Asserts semantic compatibility. Ensure both that the
         * serialization-deserialization process succeeds and that it results
         * in faithful replica of the original object.
         * <p>
         * <strong>Important points to consider:</strong>
         * <ul>
         * <li>{@code original} and {@code replica} are not necessarily equal,
         * specially of SemanticCompatibilityVerifier is used in
         * {@link SerializabilityTester#testPortableSerialization(Class, Serializable, Path, SemanticCompatibilityVerifier)}
         * since different versions of the class will be used to
         * serialize/deserialize.</li>
         * <li>If object references are used in {@code original}, the
         * reference set in {@code replica} won't be the same. Thus, unless
         * the referenced object overrides {@link #equals(Object)} and all the
         * object's attributes are part of the {@link #equals(Object)} method,
         * the following instruction will be invalid:
         * 
         * <pre>
         * Assert.assertEquals(original.getMyObject(), replica.getMyObject()); // Invalid
         * </pre>
         * 
         * Compare the reference's attributes instead:
         * 
         * <pre>
         * Assert.assertEquals(original.getMyObject().getPrimitiveAttribute_1(), replica.getMyObject().getPrimitiveAttribute_1()); // Valid
         * ...
         * Assert.assertEquals(original.getMyObject().getPrimitiveAttribute_n(), replica.getMyObject().getPrimitiveAttribute_n()); // Valid
         * 
         * Assert.assertEquals(original.getMyObject().getObjectAttribute_1().getPrimitiveAttribute_1, replica.getMyObject().getObjectAttribute_1().getPrimitiveAttribute_1()); // Valid
         * ...
         * Assert.assertEquals(original.getMyObject().getObjectAttribute_1().getPrimitiveAttribute_n, replica.getMyObject().getObjectAttribute_1().getPrimitiveAttribute_n()); // Valid
         * </pre>
         * 
         * Object value types usually override {@link #equals(Object)}
         * involving all attributes:
         * 
         * <pre>
         * Assert.assertEquals(original.getMyObjectValueType(),
         *                     replica.getMyObjectValueType()); // Valid
         * </pre>
         * 
         * </li>
         * </ul>
         * 
         * @param original original object
         * @param replica original object's replica reconstructed using
         *        deserialization
         */
        public void assertSemanticCompatibility(T original, T replica);
    }

    /**
     * Class loader able to reload classes using a different jar file.
     * <p>
     * Example taken from:
     * http://tutorials.jenkov.com/java-reflection/dynamic-
     * class-loading-reloading.html
     * <p>
     * Classes that exist in parent class loaders are used. So if you want two
     * versions of a class those classes must not be there.
     * <p>
     * Dynamic class reloading is challenging. Java's built-in Class loaders
     * always checks if a class is already loaded before loading it. Reloading
     * the class is therefore not possible using Java's built-in class
     * loaders. To reload a class you will have to implement your own
     * ClassLoader subclass.
     * <p>
     * The following is an example to load two different versions of a class
     * where none is part of the default classloader's class path:
     * 
     * <pre>
     * common.jar
     * BaseInterface
     * 
     * v1.jar
     * Hello implements BaseInterface
     * 
     * v2.jar
     * Hello implements BaseInterface
     * 
     * Program:
     * loader1 = new URLClassLoader(new URL[] {new File("v1.jar").toURL()}, Thread.currentThread().getContextClassLoader());
     * loader2 = new URLClassLoader(new URL[] {new File("v2.jar").toURL()}, Thread.currentThread().getContextClassLoader());
     * Class<?> c1 = loader1.loadClass("com.abc.Hello");
     * Class<?> c2 = loader1.loadClass("com.abc.Hello");
     * BaseInterface i1 = (BaseInterface) c1.newInstance();
     * BaseInterface i2 = (BaseInterface) c2.newInstance();
     * </pre>
     */
    private static class ClassReloader extends ClassLoader {
        private Path classpath;
        private List<String> classesToReload;

        /**
         * Reloads classes even though they have already been loaded by the
         * parent class loader.
         * 
         * @param classpath class path to the jar file used to reload classes
         *        from
         * @param parentClassLoader parent class loader used to load any other
         *        class
         * @param classesToReload classes to reload using this class loader.
         *        Any other class will be loaded using
         *        {@code parentClassLoader}. For example, if a class to reload
         *        implements {@link Serializable}, this class loader will try
         *        to first load it, however if {@link Serializable} is not
         *        part of {@code classesToReload} it will be loaded using the
         *        parentClassLoader.
         */
        public ClassReloader(Path classpath, ClassLoader parentClassLoader, String... classesToReload) {
            super(parentClassLoader);
            if (classpath == null) {
                throw new NullPointerException("classpath cannot be null");
            }

            if (!classpath.toString().toLowerCase().endsWith(".jar")) {
                throw new IllegalArgumentException("classpath must be a jar file");
            }

            if (parentClassLoader == null) {
                throw new NullPointerException("parentClassLoader cannot be null");
            }

            if(classesToReload.length == 0) {
                throw new IllegalArgumentException("If classesToReload is empty this classloader is just a regular classloader");
            }

            this.classpath = classpath;
            this.classesToReload = Arrays.asList(classesToReload);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (!this.classesToReload.contains(name)) {
                return super.loadClass(name);
            }

            try {
                String fileName = getFileName(name);
                try (JarFile jarFile = new JarFile(this.classpath.toAbsolutePath().toString())) {
                    Enumeration<JarEntry> jarEntries = jarFile.entries();
                    while (jarEntries.hasMoreElements()) {
                        JarEntry jarEntry = jarEntries.nextElement();
                        if (jarEntry.getName().equals(fileName)) {
                            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                            try (InputStream input = jarFile.getInputStream(jarEntry)) {
                                int data = input.read();
                                while (data != -1) {
                                    buffer.write(data);
                                    data = input.read();
                                }
                            }
                            byte[] classData = buffer.toByteArray();
                            return defineClass(name, classData, 0, classData.length);
                        }
                    }
                }
                throw new ClassNotFoundException("Class not found: " + name + " in path " + this.classpath);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static String getFileName(String className) {
            return className.replace(".", "/") + ".class";
        }
    }

    /**
     * Object input stream that loads classes using a custom class loader.
     */
    private static class ClassLoaderObjectInputStream extends ObjectInputStream {

        private ClassLoader classLoader;

        private ClassLoaderObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {

            try {
                String name = desc.getName();
                return Class.forName(name, false, this.classLoader);
            }
            catch (ClassNotFoundException e) {
                return super.resolveClass(desc);
            }
        }
    }
}
