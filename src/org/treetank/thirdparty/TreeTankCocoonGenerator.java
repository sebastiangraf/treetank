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

package org.treetank.thirdparty;

import org.apache.cocoon.generation.AbstractGenerator;
import org.treetank.nodelayer.IReadTransaction;
import org.treetank.nodelayer.ISession;
import org.treetank.nodelayer.Session;
import org.treetank.xmllayer.SAXGenerator;
import org.xml.sax.SAXException;


/**
 * <h1>TreeTankCocoonGenerator</h1>
 * 
 * <p>Cocoon generator producing SAX events from an idefix storage. The idefix 
 * file is specified by the 'src' attribute of the 'generate' element of the 
 * pipeline.</p>
 */
public class TreeTankCocoonGenerator extends AbstractGenerator {

  /**
   * Generate SAX events by traversing whole idefix storage starting at root.
   * 
   * @throws SAXException in case idefix encounters an error.
   */
  public void generate() throws SAXException {

    try {

      final ISession session = Session.getSession(source);
      final IReadTransaction trx = session.beginReadTransaction();
      final SAXGenerator generator = new SAXGenerator(trx, contentHandler);
      generator.start();
      generator.join();

    } catch (Exception e) {
      throw new SAXException(e);
    }

  }

}
