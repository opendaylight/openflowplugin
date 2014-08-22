/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import org.junit.Test;

import org.opendaylight.util.junit.EqualityTester.Exerciser;
import org.opendaylight.util.junit.ThrowableTester.Instruction;
import org.opendaylight.util.junit.ThrowableTester.Validator;

/**
 * Test for {@link org.opendaylight.util.junit.EqualityTester}
 * 
 * @author Fabiel Zuniga
 */
public class EqualityTesterTest {

    @Test
    public void testTestEqualsAndHashCode() {
        final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

        long partofEquals = randomDataGenerator.getLong();
        Equable base = new Equable(partofEquals, randomDataGenerator.getLong());
        Equable equalsToBase1 = new Equable(partofEquals,
                                            randomDataGenerator.getLong());
        Equable equalsToBase2 = new Equable(partofEquals,
                                            randomDataGenerator.getLong());
        Equable unequalToBase = new Equable(randomDataGenerator.getLong(),
                                            randomDataGenerator.getLong());

        EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                             equalsToBase2, unequalToBase);
    }

    @Test
    public void testTestEqualsAndHashCodeWithExerciser() {
        final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

        long partofEquals = randomDataGenerator.getLong();
        Equable base = new Equable(partofEquals, randomDataGenerator.getLong());
        Equable equalsToBase1 = new Equable(partofEquals,
                                            randomDataGenerator.getLong());
        Equable equalsToBase2 = new Equable(partofEquals,
                                            randomDataGenerator.getLong());
        Equable unequalToBase = new Equable(randomDataGenerator.getLong(),
                                            randomDataGenerator.getLong());

        Exerciser<Equable> exerciser = new Exerciser<Equable>() {

            @Override
            public void exercise(Equable obj) {
                obj.setNotPartOfEquals(randomDataGenerator.getLong());
            }
        };

        EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                             equalsToBase2, exerciser,
                                             unequalToBase);
    }

    @Test
    public void testNoReflexive() {
        final EquableNoReflexive base = new EquableNoReflexive(Id.BASE);
        final EquableNoReflexive equalsToBase1 = new EquableNoReflexive(
                                                                        Id.EQUALS_TO_BASE_1);
        final EquableNoReflexive equalsToBase2 = new EquableNoReflexive(
                                                                        Id.EQUALS_TO_BASE_2);
        final EquableNoReflexive unequalToBase = new EquableNoReflexive(
                                                                        Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Reflexive property broken";
                TestTools.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                                     equalsToBase2,
                                                     unequalToBase);
            }
        }, errorValidator);
    }

    @Test
    public void testNoSymmetric() {
        final EquableNoSymmetric base = new EquableNoSymmetric(Id.BASE);
        final EquableNoSymmetric equalsToBase1 = new EquableNoSymmetric(
                                                                        Id.EQUALS_TO_BASE_1);
        final EquableNoSymmetric equalsToBase2 = new EquableNoSymmetric(
                                                                        Id.EQUALS_TO_BASE_2);
        final EquableNoSymmetric unequalToBase = new EquableNoSymmetric(
                                                                        Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Symmetric property broken";
                TestTools.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                                     equalsToBase2,
                                                     unequalToBase);
            }
        }, errorValidator);
    }

    @Test
    public void testNoTransitive() {
        final EquableNoTransitive base = new EquableNoTransitive(Id.BASE);
        final EquableNoTransitive equalsToBase1 = new EquableNoTransitive(
                                                                          Id.EQUALS_TO_BASE_1);
        final EquableNoTransitive equalsToBase2 = new EquableNoTransitive(
                                                                          Id.EQUALS_TO_BASE_2);
        final EquableNoTransitive unequalToBase = new EquableNoTransitive(
                                                                          Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Transitive property broken";
                TestTools.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                                     equalsToBase2,
                                                     unequalToBase);
            }
        }, errorValidator);
    }

    @Test
    public void testNullFailure() {
        final EquableNullFailure base = new EquableNullFailure(Id.BASE);
        final EquableNullFailure equalsToBase1 = new EquableNullFailure(
                                                                        Id.EQUALS_TO_BASE_1);
        final EquableNullFailure equalsToBase2 = new EquableNullFailure(
                                                                        Id.EQUALS_TO_BASE_2);
        final EquableNullFailure unequalToBase = new EquableNullFailure(
                                                                        Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Null reference property broken";
                TestTools.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                                     equalsToBase2,
                                                     unequalToBase);
            }
        }, errorValidator);
    }

    @Test
    public void testDifferentTypeFailure() {
        final EquableDifferentTypeFailure base = new EquableDifferentTypeFailure(
                                                                                 Id.BASE);
        final EquableDifferentTypeFailure equalsToBase1 = new EquableDifferentTypeFailure(
                                                                                          Id.EQUALS_TO_BASE_1);
        final EquableDifferentTypeFailure equalsToBase2 = new EquableDifferentTypeFailure(
                                                                                          Id.EQUALS_TO_BASE_2);
        final EquableDifferentTypeFailure unequalToBase = new EquableDifferentTypeFailure(
                                                                                          Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Different type parameter consideration broken";
                TestTools.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                                     equalsToBase2,
                                                     unequalToBase);
            }
        }, errorValidator);
    }

    @Test
    public void testInequalityFailure() {
        final EquableInequalityFailure base = new EquableInequalityFailure(
                                                                           Id.BASE);
        final EquableInequalityFailure equalsToBase1 = new EquableInequalityFailure(
                                                                                    Id.EQUALS_TO_BASE_1);
        final EquableInequalityFailure equalsToBase2 = new EquableInequalityFailure(
                                                                                    Id.EQUALS_TO_BASE_2);
        final EquableInequalityFailure unequalToBase = new EquableInequalityFailure(
                                                                                    Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Inequality test broken";
                TestTools.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                                     equalsToBase2,
                                                     unequalToBase);
            }
        }, errorValidator);
    }

    @Test
    public void testHashcodeFailure() {
        final EquableHashcodeFailure base = new EquableHashcodeFailure(Id.BASE);
        final EquableHashcodeFailure equalsToBase1 = new EquableHashcodeFailure(
                                                                                Id.EQUALS_TO_BASE_1);
        final EquableHashcodeFailure equalsToBase2 = new EquableHashcodeFailure(
                                                                                Id.EQUALS_TO_BASE_2);
        final EquableHashcodeFailure unequalToBase = new EquableHashcodeFailure(
                                                                                Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Hashcode broken";
                TestTools.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                                     equalsToBase2,
                                                     unequalToBase);
            }
        }, errorValidator);
    }

    @Test
    public void testNoConsistent() {
        final EquableNoConsistent base = new EquableNoConsistent(Id.BASE);
        final EquableNoConsistent equalsToBase1 = new EquableNoConsistent(
                                                                          Id.EQUALS_TO_BASE_1);
        final EquableNoConsistent equalsToBase2 = new EquableNoConsistent(
                                                                          Id.EQUALS_TO_BASE_2);
        final EquableNoConsistent unequalToBase = new EquableNoConsistent(
                                                                          Id.UNEQALS_TO_BASE);

        final Exerciser<EquableNoConsistent> exerciser = new Exerciser<EquableNoConsistent>() {

            @Override
            public void exercise(EquableNoConsistent obj) {
                obj.brakeConsistency();
            }
        };

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Consistent property broken for";
                TestTools.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                                     equalsToBase2, exerciser,
                                                     unequalToBase);
            }
        }, errorValidator);
    }

    @Test
    public void testHashcodeNoConsistent() {
        final EquableHashcodeNoConsistent base = new EquableHashcodeNoConsistent(
                                                                                 Id.BASE);
        final EquableHashcodeNoConsistent equalsToBase1 = new EquableHashcodeNoConsistent(
                                                                                          Id.EQUALS_TO_BASE_1);
        final EquableHashcodeNoConsistent equalsToBase2 = new EquableHashcodeNoConsistent(
                                                                                          Id.EQUALS_TO_BASE_2);
        final EquableHashcodeNoConsistent unequalToBase = new EquableHashcodeNoConsistent(
                                                                                          Id.UNEQALS_TO_BASE);

        final Exerciser<EquableHashcodeNoConsistent> exerciser = new Exerciser<EquableHashcodeNoConsistent>() {

            @Override
            public void exercise(EquableHashcodeNoConsistent obj) {
                obj.brakeConsistency();
            }
        };

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Hashcode consistent property broken";
                TestTools.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(base, equalsToBase1,
                                                     equalsToBase2, exerciser,
                                                     unequalToBase);
            }
        }, errorValidator);
    }

    private static class Equable {

        private long partOfEquals;

        @SuppressWarnings("unused")
        private long notPartOfEquals;

        public Equable(long partOfEquals, long notPartOfEquals) {
            this.partOfEquals = partOfEquals;
            this.notPartOfEquals = notPartOfEquals;
        }

        public void setNotPartOfEquals(long notPartOfEquals) {
            this.notPartOfEquals = notPartOfEquals;
        }

        @Override
        public int hashCode() {
            return Long.valueOf(this.partOfEquals).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            Equable other = (Equable) obj;

            if (this.partOfEquals != other.partOfEquals) {
                return false;
            }

            return true;
        }
    }

    private static enum Id {
        BASE, EQUALS_TO_BASE_1, EQUALS_TO_BASE_2, UNEQALS_TO_BASE;
    }

    private static abstract class BrokenEquable {

        private Id id;

        public BrokenEquable(Id id) {
            this.id = id;
        }

        protected Id getId() {
            return this.id;
        }

        @Override
        public int hashCode() {
            if (this.id == Id.UNEQALS_TO_BASE) {
                return 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (!(obj instanceof BrokenEquable)) {
                return false;
            }

            BrokenEquable other = (BrokenEquable) obj;

            if (this.id == Id.UNEQALS_TO_BASE) {
                return false;
            }

            if (other.id == Id.UNEQALS_TO_BASE) {
                return false;
            }

            return true;
        }
    }

    private static class EquableNoReflexive extends BrokenEquable {

        public EquableNoReflexive(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return false; // Invalid
            }
            return super.equals(obj);
        }
    }

    private static class EquableNoSymmetric extends BrokenEquable {

        public EquableNoSymmetric(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof BrokenEquable)) {
                return false;
            }

            BrokenEquable other = (BrokenEquable) obj;

            if (getId() == Id.BASE && other.getId() == Id.EQUALS_TO_BASE_1) {
                return true;
            }

            if (getId() == Id.EQUALS_TO_BASE_1 && other.getId() == Id.BASE) {
                return false;
            }

            return super.equals(obj);
        }
    }

    private static class EquableNoTransitive extends BrokenEquable {

        public EquableNoTransitive(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof BrokenEquable)) {
                return false;
            }

            BrokenEquable other = (BrokenEquable) obj;

            if (getId() == Id.BASE && other.getId() == Id.EQUALS_TO_BASE_1) {
                return true;
            }

            if (getId() == Id.EQUALS_TO_BASE_1
                    && other.getId() == Id.EQUALS_TO_BASE_2) {
                return true;
            }

            if (getId() == Id.BASE && other.getId() == Id.EQUALS_TO_BASE_2) {
                return false;
            }

            return super.equals(obj);
        }
    }

    private static class EquableNullFailure extends BrokenEquable {

        public EquableNullFailure(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return true;
            }

            return super.equals(obj);
        }
    }

    private static class EquableDifferentTypeFailure extends BrokenEquable {

        public EquableDifferentTypeFailure(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof EquableDifferentTypeFailure)) {
                return true;
            }

            return super.equals(obj);
        }
    }

    private static class EquableInequalityFailure extends BrokenEquable {

        public EquableInequalityFailure(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof EquableInequalityFailure)) {
                return false;
            }

            EquableInequalityFailure other = (EquableInequalityFailure) obj;

            if (other.getId() == Id.UNEQALS_TO_BASE) {
                return true;
            }

            return super.equals(obj);
        }
    }

    private static class EquableHashcodeFailure extends BrokenEquable {

        public EquableHashcodeFailure(Id id) {
            super(id);
        }

        @Override
        public int hashCode() {
            if (getId() == Id.BASE) {
                return 0;
            } else if (getId() == Id.EQUALS_TO_BASE_1) {
                return 1;
            } else if (getId() == Id.EQUALS_TO_BASE_2) {
                return 2;
            }

            return super.hashCode();
        }
    }

    private static class EquableNoConsistent extends BrokenEquable {

        private boolean brakeConsistency = false;

        public EquableNoConsistent(Id id) {
            super(id);
        }

        public void brakeConsistency() {
            this.brakeConsistency = true;
        }

        @Override
        public boolean equals(Object obj) {
            if (this.brakeConsistency) {
                return false;
            }

            return super.equals(obj);
        }
    }

    private static class EquableHashcodeNoConsistent extends BrokenEquable {

        private boolean brakeConsistency = false;

        public EquableHashcodeNoConsistent(Id id) {
            super(id);
        }

        public void brakeConsistency() {
            this.brakeConsistency = true;
        }

        @Override
        public int hashCode() {
            if (this.brakeConsistency) {
                return getId().hashCode();
            }
            return super.hashCode();
        }
    }
}
