/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.xmllayer;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.perfidix.BenchClass;
import org.perfidix.Benchmark;
import org.perfidix.visitor.AsciiTable;

@BenchClass(runs = 3)
public class CocoonBenchmark {

  private static final int RUNS = 10;

  class XMLThread extends Thread {
    private final String mQuery;

    public XMLThread(final String query) {
      mQuery = query;
    }

    public void run() {
      try {
        final HttpClient client = new HttpClient();
        final GetMethod method = new GetMethod(mQuery);
        client.executeMethod(method);
      } catch (HttpException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public void benchXML() throws Exception {
    XMLThread[] t = new XMLThread[RUNS];
    for (int i = 0; i < RUNS; i++) {
      t[i] = new XMLThread("http://localhost:8888/treetank/shakespeare?filter=1");
      t[i].start();
    }
    for (int i = 0; i < RUNS; i++) {
      t[i].join();
    }
  }

  public void benchTreeTank() throws Exception {
    XMLThread[] t = new XMLThread[RUNS];
    for (int i = 0; i < RUNS; i++) {
      t[i] =
          new XMLThread(
              "http://localhost:8888/treetank/xpath?file=shakespeare.tnk&query=/PLAYS/PLAY[8]//LINE/text()&filter=1");
      t[i].start();
    }
    for (int i = 0; i < RUNS; i++) {
      t[i].join();
    }
  }

  public void benchExist() throws Exception {
    XMLThread[] t = new XMLThread[RUNS];
    for (int i = 0; i < RUNS; i++) {
      t[i] = new XMLThread("http://localhost:8080/exist/kramis?filter=1");
      t[i].start();
    }
    for (int i = 0; i < RUNS; i++) {
      t[i].join();
    }
  }

  public static void main(final String[] args) {
    System.out
        .println("$Id:CocoonBenchmark.java 1617 2006-10-12 17:32:13Z kramis $");
    try {
      Benchmark a = new Benchmark();
      CocoonBenchmark bench = new CocoonBenchmark();
      a.add(bench);
      org.perfidix.Result r = a.run();
      AsciiTable v = new AsciiTable();
      v.visit(r);
      System.out.println(v.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
