/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import org.opendaylight.util.junit.SerializabilityTester.SemanticCompatibilityVerifier;
import org.opendaylight.util.junit.ThrowableTester.Instruction;
import org.opendaylight.util.junit.ThrowableTester.Validator;

/**
 * Test for {@link org.opendaylight.util.junit.SerializabilityTester}
 * 
 * @author Fabiel Zuniga
 */
public class SerializabilityTesterTest {

    private static final Path PREVIOUS_VERSION_JAR_PATH = FileSystems.getDefault().getPath("src", "test", "resources",
            "portable-serialization-test", "previous-version.jar");

    @Test
    public void testTestSerialization() {
        PortableClass serializable = new PortableClass();
        serializable.setAttrPreviousVersion("previous attr");
        serializable.setAttrCurrentVersion("current attr");

        // Test binary and semantic compatibility

        SemanticCompatibilityVerifier<PortableClass> semanticVerifier = new SemanticCompatibilityVerifier<PortableClass>() {
            @Override
            public void assertSemanticCompatibility(PortableClass original, PortableClass replica) {
                // System.out.println(original);
                // System.out.println(replica);

                Assert.assertEquals(original.getAttrPreviousVersion(), replica.getAttrPreviousVersion());
                Assert.assertEquals(original.getAttrCurrentVersion(), replica.getAttrCurrentVersion());
            }
        };

        SerializabilityTester.testSerialization(serializable, semanticVerifier);

    }

    @Test
    public void testTestSerializationFailure() {
        final InvalidSerializable serializable = new InvalidSerializable();

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Serialization failure:";
                TestTools.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                SerializabilityTester.testSerialization(serializable, null);
            }
        }, errorValidator);
    }

    @Ignore("figure out the test jar thing")
    @Test
    public void testTestPortableSerialization() throws Exception {
        SemanticCompatibilityVerifier<PortableClass> semanticVerifier = new SemanticCompatibilityVerifier<PortableClass>() {
            @Override
            public void assertSemanticCompatibility(PortableClass original, PortableClass replica) {
                Assert.assertEquals(original.getAttrPreviousVersion(), replica.getAttrPreviousVersion());
                Assert.assertNotNull(original.getAttrCurrentVersion());
                Assert.assertNull(replica.getAttrCurrentVersion());
            }
        };

        PortableClass currentVersion = new PortableClass();
        currentVersion.setAttrPreviousVersion("previous version value at: " + new Date());
        currentVersion.setAttrCurrentVersion("current version value at : " + new Date());

        SerializabilityTester.testPortableSerialization(PortableClass.class, currentVersion, PREVIOUS_VERSION_JAR_PATH,
                semanticVerifier);
    }

    private static class InvalidSerializable implements Serializable {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unused")
        private Object object = new Object();
    }
}
