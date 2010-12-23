package com.treetank.service.xml.xpath.xmark;

import com.treetank.exception.TTXPathException;

import org.perfidix.annotation.AfterBenchClass;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeBenchClass;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.Bench;

public class XMarkBenchTestPerfidix {

    XMarkBenchTest xmbt = new XMarkBenchTest();;

    @BeforeEachRun
    public void setUp() {
        xmbt.setUp();
    }

    @Bench
    public void testXMark_Q1() {
        try {
            xmbt.xMarkTest_Q1();
        } catch (TTXPathException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Bench
    public void testXMark_Q5() {
        try {
            xmbt.xMarkTest_Q5();
        } catch (TTXPathException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Bench
    public void testXMark_Q6() {
        try {
            xmbt.xMarkTest_Q6();
        } catch (TTXPathException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Bench
    public void testXMark_Q7() {
        try {
            xmbt.xMarkTest_Q7();
        } catch (TTXPathException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // @Bench
    // public void testXMark_21() {
    // xmbt.xMarkTest_Q21();
    // }
    //
    // @Bench
    // public void testXMark_22() {
    // xmbt.xMarkTest_Q22();
    // }
    //
    // @Bench
    // public void testXMark_23() {
    // xmbt.xMarkTest_Q23();
    // }

    @AfterEachRun
    public void tearDownTest() {
        xmbt.tearDown();

    }

}
