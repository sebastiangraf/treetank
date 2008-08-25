/*
 * Copyright (c) 2007, Marc Kramis
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id:readme.txt 4014 2008-03-26 16:36:35Z kramis $
 */
 
TreeTank Development Environment
--------------------------------
+ J2SE 5.
+ JUnit 4.4.
+ Checkstyle 4.4.
+ Eclipse 3.3.2.
+ Subclipse 1.2.4.
+ Spell Checker.
+ JamVM 1.5.1.
+ Classpath 0.92.

TreeTank Coding Conventions
---------------------------
+ Each source file belongs to exactly one author and must be set according to:
  Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
+ Each source file must sport the ICS license header as defined in license.txt.
+ Apply project-specific formatter and import organizer to file before save.
+ File encoding: UTF-8.
+ File line delimiter: Unix.
+ Displayed tab width: 2.
+ Spaces for tab: yes.
+ Subversion, JavaDoc, and other comments: Correct English sentences.

+ Implement according to roadmap.txt which is defined by the project lead.
+ Test driven development: Write test cases named to **/*Test.java
+ Change management: Append changes to changes.txt and increment revision
  in build.xml.
+ Team collaboration: Checkout or update must not yield errors or failing tests.
+ Trunk maintenance: Work on branch until the project lead agrees to merge.
+ Dependencies: No external dependencies (jar files) except 
  JUnit, Checkstyle, StAX, Jetty, and Concurrent Backport.
+ The source and test code must be executable with jamvm at any time.
+ The folder target may only contain libTreeTank.so and treetank.jar. Whenever
  the version of treetank changes, make sure to update these two files and only
  commit when they are updated.
