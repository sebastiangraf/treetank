package com.treetank.service.xml.xpath;

import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.BeforeFirstRun;
import org.perfidix.annotation.Bench;
import org.perfidix.annotation.BenchClass;

import com.treetank.exception.TreetankException;

public class XMarkBenchTestPerfidix {

	@BeforeEachRun
	public void createDb() {
		try {
			new XMarkBenchTest().setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Bench(runs = 1)
	public void testXMark_Q1() {
		try {
			new XMarkBenchTest().xMarkTest_Q1();
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

//	@Bench(runs = 3)
//	public void testXMark_Q5() {
//		// new XMarkBenchTest().xMarkTest_Q5();
//	}

	@AfterEachRun
	public void tearDownTest() {
		try {
			new XMarkBenchTest().tearDown();
		} catch (TreetankException e) {
			e.printStackTrace();
		}
	}

}
