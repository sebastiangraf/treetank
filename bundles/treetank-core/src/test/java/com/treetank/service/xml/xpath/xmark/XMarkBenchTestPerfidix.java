package com.treetank.service.xml.xpath.xmark;

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
		xmbt.xMarkTest_Q1();
	}

	@Bench
	public void testXMark_Q5() {
		xmbt.xMarkTest_Q5();
	}

	@Bench
	public void testXMark_Q6() {
		xmbt.xMarkTest_Q6();
	}

	@Bench
	public void testXMark_Q7() {
		xmbt.xMarkTest_Q7();
	}
	
	//@Bench
	//public void testXMark_21() {
	//	xmbt.xMarkTest_Q21();
	//}
	//
//	@Bench
//	public void testXMark_22() {
//		xmbt.xMarkTest_Q22();
//	}
//
//	@Bench
//	public void testXMark_23() {
//		xmbt.xMarkTest_Q23();
//	}
	
	@AfterEachRun
	public void tearDownTest() {
		xmbt.tearDown();

	}

}
