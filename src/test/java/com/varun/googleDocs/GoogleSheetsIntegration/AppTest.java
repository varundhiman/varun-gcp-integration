package com.varun.googleDocs.GoogleSheetsIntegration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     * name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        final A a = new A();
        final B b = new B();
        final A castedA = b;
        final B castedB = (B) a;
        assertTrue(true);
    }
}

class A {

}

class B extends A {

}
