/*
 * (c) Copyright 2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.format;

import static org.opendaylight.util.format.InvalidTokenTranslator.RETURN_EMPTY_STRING;
import static org.opendaylight.util.format.InvalidTokenTranslator.RETURN_NULL;
import static org.opendaylight.util.format.InvalidTokenTranslator.RETURN_TOKEN_STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.junit.Test;

/**
 * Unit tests for the token message translator and related classes.
 * 
 * @author Thomas Vachuska
 */
public class FormatTest {

    private void verify(TokenMessageTranslator mt, TokenTranslator tt,
                        String m, String e) {
        try {
            String r = mt.translate(m, tt);
            assertEquals("Translation failed!", e, r);
        } catch (Error ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private void verify(TokenMessageTranslator mt, TokenTranslator tt) {
        verify(mt, tt, "%", "%"); // token flag by itself is no token
        verify(mt, tt, "%{}", ""); // empty token
        verify(mt, tt, "%{}%", "%"); // empty token variation
        verify(mt, tt, "%{a}", "A");
        verify(mt, tt, "%a", "A");
        verify(mt, tt, "%%a", "%a");
        verify(mt, tt, "%%a ", "%a ");
        verify(mt, tt, "%%a%", "%a%");
        verify(mt, tt, "%%a%%", "%a%");
        verify(mt, tt, "%{abc}", "ABC");
        verify(mt, tt, "%abc", "ABC");
        verify(mt, tt, "%{a}%{b}", "AB");
        verify(mt, tt, "%{abc}%{def}", "ABCDEF");
        verify(mt, tt, "%abc%def", "ABCDEF");
        verify(mt, tt, "%{abc}%{def}%", "ABCDEF%");
        verify(mt, tt, "%abc%def%", "ABCDEF%");
        verify(mt, tt, "%{abc} %{def}%", "ABC DEF%");
        verify(mt, tt, "%abc %def%", "ABC DEF%");
        verify(mt, tt, "%abc;%def;", "ABC;DEF;");
        verify(mt, tt, "%{abc};%{def};", "ABC;DEF;");
    }

    /**
     * This method is used to verify the RestrictedTokenMessageTranslator
     * @param mt message translator
     * @param tt token translator
     * @param testInvalidTokens true to test invalid tokens
     */
    private void restrictedVerify(TokenMessageTranslator mt,
                                  TokenTranslator tt, boolean testInvalidTokens) {

        verify(mt, tt, "%", "%"); // token flag by itself is no token
        verify(mt, tt, "%{}", "%{}"); // empty token
        verify(mt, tt, "%{a}", "A");
        verify(mt, tt, "%%{a}", "%%{a}");
        verify(mt, tt, "%%{a}%", "%%{a}%");
        verify(mt, tt, "%%{a}%%", "%%{a}%%");
        verify(mt, tt, "%%abcd%%abcd", "%%abcd%%abcd");
        verify(mt, tt, "%{abc}", "ABC");
        verify(mt, tt, "%{a}%{b}", "AB");
        verify(mt, tt, "%{abc}%{def}", "ABCDEF");
        verify(mt, tt, "%{abc}%{def}%", "ABCDEF%");
        verify(mt, tt, "%{abc} %{def}%", "ABC DEF%");
        verify(mt, tt, "%{abc};%{def};", "ABC;DEF;");

        if (testInvalidTokens) {
            verify(mt, tt, "%", "%"); // token flag by itself is no token
            verify(mt, tt, "%{}", "%{}"); // empty token
            verify(mt, tt, "%{ab}", "%{ab}");
            verify(mt, tt, "%%{a}", "%%{a}");
            verify(mt, tt, "%%{a}%", "%%{a}%");
            verify(mt, tt, "%%{a}%%", "%%{a}%%");
            verify(mt, tt, "%%abcd%%abcd", "%%abcd%%abcd");
            verify(mt, tt, "%{abcd}", "%{abcd}");
            verify(mt, tt, "%{ab}%{ba}", "%{ab}%{ba}");
            verify(mt, tt, "%{foo}%{bar}", "%{foo}%{bar}");
            verify(mt, tt, "%{abc!}%{def}%", "%{abc!}DEF%");
            verify(mt, tt, "%{abc} %{foo}%", "ABC %{foo}%");
            verify(mt, tt, "%%{_};%{def};", "%%{_};DEF;");
        }
    }

    private static class TestTranslator implements TokenTranslator {
        @Override
        public String translate(String token) {
            return token.toUpperCase();
        }
    }

    @Test
    public void testSimpleTokenMessageTranslator1() {
        TokenMessageTranslator mt = new SimpleTokenMessageTranslator();
        TokenTranslator tt = new TestTranslator();
        verify(mt, tt);
    }

    @Test
    public void testRestrictedTokenMessageTranslator1() {
        TokenMessageTranslator mt = new RestrictedTokenMessageTranslator();
        TokenTranslator tt = new TestTranslator();
        restrictedVerify(mt, tt, false);
    }

    public void testResourceBundleTranslator(TokenTranslator tt) {
        TokenMessageTranslator mt = new RestrictedTokenMessageTranslator();
        restrictedVerify(mt, tt, true);

        verify(mt, tt, "%{number_en:42}", "Hub(s)-42");
        verify(mt, tt, "%{number_re:42}", "42-(s)buH");
        verify(mt, tt, "%{number_en:}", "Hub(s)-{0}");
        verify(mt, tt, "%{number_en}", "Hub(s)-{0}");
        verify(mt, tt, "%{number_en}", "Hub(s)-{0}");
        verify(mt, tt, "%{mftest1:1,2,3}", "The order is 1, 2, 3!");
        verify(mt, tt, "%{mftest2:1,2,3}", "The reversed order is 3, 2, 1!");

    }

    @Test
    public void testResourceBundleTranslator1() {
        ResourceBundleTokenTranslator tt = 
            new ResourceBundleTokenTranslator(ResourceBundle.getBundle("org.opendaylight.util.format.FormatTest"),
                                              true);
        assertTrue(tt.getUseMessageFormat());
        tt.setKeySeparator(":");
        tt.setValueSeparator(",");
        testResourceBundleTranslator(tt);
    }

    @Test
    public void testResourceBundleTranslator2() {
        ResourceBundleTokenTranslator tt = 
            new ResourceBundleTokenTranslator(ResourceBundle.getBundle("org.opendaylight.util.format.FormatTest"));
        tt.setUseMessageFormat(true);
        assertTrue(tt.getUseMessageFormat());
        testResourceBundleTranslator(tt);
    }

    @Test
    public void testResourceBundleTranslator3() {
        ResourceBundleTokenTranslator tt = 
            new ResourceBundleTokenTranslator(ResourceBundle.getBundle("org.opendaylight.util.format.FormatTest"),
                                              InvalidTokenTranslator.RETURN_NULL);
        tt.setUseMessageFormat(true);
        assertTrue(tt.getUseMessageFormat());
        testResourceBundleTranslator(tt);
    }

    @Test
    public void testResourceBundleTranslator4() {
        ResourceBundleTokenTranslator tt = 
            new ResourceBundleTokenTranslator(ResourceBundle.getBundle("org.opendaylight.util.format.FormatTest"),
                                              true, InvalidTokenTranslator.RETURN_NULL);
        assertTrue(tt.getUseMessageFormat());
        testResourceBundleTranslator(tt);
    }

    @Test
    public void testMapTranslator1() {
        TokenMessageTranslator mt = new SimpleTokenMessageTranslator();
        Map<String, Object> testMap = new HashMap<String, Object>();
        testMap.put("a", "A");
        testMap.put("b", "B");
        testMap.put("abc", "ABC");
        testMap.put("def", "DEF");

        MapTranslator tt = new MapTranslator(testMap,RETURN_EMPTY_STRING);
        verify(mt, tt);
        assertEquals("incorrect behaviour:", RETURN_EMPTY_STRING, tt.getBehaviour());
    }

    @Test
    public void testMapTranslator2() {
        TokenMessageTranslator mt = new SimpleTokenMessageTranslator();
        Object[][] objects = new Object[][] { { "a", "A" }, { "b", "B" },
                { "abc", "ABC" }, { "def", "DEF" } };

        MapTranslator tt = new MapTranslator(objects);
        tt.setBehaviour(RETURN_EMPTY_STRING);
        verify(mt, tt);
        assertEquals("incorrect behaviour:", RETURN_EMPTY_STRING, tt.getBehaviour());

        tt.setBehaviour(RETURN_NULL);
        verify(mt, tt, "testing %foo%bar...%a%abc%def",
               "testing %foo%bar...AABCDEF");
        assertEquals("incorrect behaviour:", RETURN_NULL, tt.getBehaviour());
    }

    @Test
    public void testMapTranslator3() {
        TokenMessageTranslator mt = new SimpleTokenMessageTranslator();
        Object[] objects = new Object[] { " ", "A", "B", "C", "D", "E", "F",
                "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
                "S", "T", "U", "V", "W", "X", "Y", "Z" };

        MapTranslator tt = new MapTranslator(objects);
        verify(mt, tt, "testing %1%2%3...%24%25%26%27", "testing ABC...XYZ%27");
        assertEquals("incorrect behaviour:", RETURN_NULL, tt.getBehaviour());

        tt.setBehaviour(RETURN_EMPTY_STRING);
        verify(mt, tt, "testing %1%2%3...%24%25%26%27", "testing ABC...XYZ");
        assertEquals("incorrect behaviour:", RETURN_EMPTY_STRING,
                     tt.getBehaviour());

        tt.setBehaviour(RETURN_TOKEN_STRING);
        verify(mt, tt, "testing %1%2%3...%24%25%26%27", "testing ABC...XYZ27");
        assertEquals("incorrect behaviour:", RETURN_TOKEN_STRING,tt.getBehaviour());
    }


    @Test
    public void testFormatUtils() {
        TokenMessageTranslator mt = new RestrictedTokenMessageTranslator();
        TokenTranslator tt = new ResourceBundleTokenTranslator(ResourceBundle.getBundle("org.opendaylight.util.format.FormatTest"), true);

        String testString;

        // First test a simple string
        testString = FormatUtils.constructTokenMessage("mftest1", new String[] {
                "a", "b", "c" });
        assertEquals("FormatUtils: construction failed", testString,
                     "%{mftest1:a,b,c}");
        verify(mt, tt, testString, "The order is a, b, c!");

        // Next text a more complex one... with various tricky cases
        // Note: \u201A is a unicode "comma" (Decimal = 8218).
        testString = FormatUtils.constructTokenMessage("mftest1", new String[] {
                "\u6B61\u0000", "\nhappy::,::code\n", "{foo bar}" });
        assertEquals("FormatUtils: construction failed;", testString,
                     "%{mftest1:\u6B61 , happy::\u201A::code ,[foo bar]}");

        verify(mt, tt, testString,
               "The order is \u6B61 ,  happy::\u201A::code , [foo bar]!");
    }

    
    static class Animal {

        private boolean isMale;

        private int age;

        Animal(boolean isMale, int age) {
            this.isMale = isMale;
            this.age = age;
        }

        public int getAge() {
            return age;
        }

        public boolean isMale() {
            return isMale;
        }

        public String getGender() {
            return isMale ? "male" : "female";
        }
    }

    static class Person extends Animal {

        private String name;

        private Person mother;

        private Person father;

        private Person spouse;

        private List<Person> children = new ArrayList<Person>();

        Person(boolean isMale, String name, int age, Person mother,
               Person father) {
            super(isMale, age);
            this.name = name;
            this.mother = mother;
            if (mother != null)
                mother.children.add(this);
            this.father = father;
            if (father != null)
                father.children.add(this);
        }

        public String getName() {
            return name;
        }

        public Person getMother() {
            return mother;
        }

        public Person getFather() {
            return father;
        }

        public Person getSpouse() {
            return spouse;
        }

        public void setSpouse(Person p) {
            this.spouse = p;
            p.spouse = this;
        }

        public List<Person> getChildren() {
            return children;
        }

        public void addChild(Person p) {
            children.add(p);
            if (isMale()) {
                p.father = this;
            } else {
                p.mother = this;
            }
        }

        public boolean isMarried() {
            return spouse != null;
        }
    }

    @Test
    public void testReflectionTranslator() {
        TokenMessageTranslator mt = new SimpleTokenMessageTranslator('$');

        Person m = new Person(false, "momma bear", 64, null, null);
        Person f = new Person(true, "pappa bear", 66, null, null);
        m.setSpouse(f);

        Person a = new Person(true, "brother bear", 8, m, f);
        Person b = new Person(false, "sister bear", 4, m, f);

        ReflectionTranslator rt = new ReflectionTranslator(a);

        verify(mt, rt, "${getMother.getName.toUpperCase} and "
                + "${getFather.getName.toUpperCase} "
                + "had a ${getGender} baby ${getName.toUpperCase} "
                + "${getAge} years ago.",
               "MOMMA BEAR and PAPPA BEAR had a male baby BROTHER BEAR "
                       + "8 years ago.");

        verify(mt, rt, "${getMother.getName.toUpperCase} and "
                + "${getFather.getName.toUpperCase} "
                + "had ${getMother.getChildren.size} babies. "
                + "The oldest one is a $getGender named "
                + "${getMother.getChildren.iterator.next.getName.toUpperCase} "
                + "age ${getMother.getChildren.iterator.next.getAge}.",
               "MOMMA BEAR and PAPPA BEAR had 2 babies. The oldest one is a "
                       + "male named BROTHER BEAR age 8.");

        rt.setObject(b);
        verify(mt, rt, "${getMother.getName.toUpperCase} and "
                + "${getFather.getName.toUpperCase} "
                + "had some more fun and then had a ${getGender} baby "
                + "${getName.toUpperCase} ${getAge} years ago.",
               "MOMMA BEAR and PAPPA BEAR had some more fun and then had "
                       + "a female baby SISTER BEAR 4 years ago.");

        verify(mt, rt, "${getClass.getName}", b.getClass().getName());

        verify(mt, rt, "${getSpouse}", "${getSpouse}");
        rt.setBehaviour(InvalidTokenTranslator.RETURN_EMPTY_STRING);
        verify(mt, rt, "${getSpouse}", "");
        rt.setEmptyString("*");
        verify(mt, rt, "${getSpouse}", "*");
        rt.setBehaviour(InvalidTokenTranslator.RETURN_TOKEN_STRING);
        verify(mt, rt, "${getSpouse}", "getSpouse");

        assertEquals("incorrect mode",
                     InvalidTokenTranslator.RETURN_TOKEN_STRING, rt
                         .getBehaviour());
        assertEquals("incorrect empty string", "*", rt.getEmptyString());
    }

}
