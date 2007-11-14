/*
 * Copyright 2007, Marc Kramis
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * $Id$
 */

package org.treetank.beanlayer;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.UTF;

/**
 * <h1>BeanUtil</h1>
 * 
 * <p>
 * Bean util to write and read Java beans to and from a TreeTank.
 * </p>
 * 
 * <p>
 * <strong>Example</strong>
 * 
 * <pre>
 * public class Address {
 *   // Bean properties.
 *   &#64;
 *   private long id;
 *   
 *   &#64;FullText
 *   private String firstName;
 *   
 *   &#64;FullText
 *   private String lastName;
 *   
 *   private String street;
 *   
 *   // Bean setters and getters.
 *   public void setFirstName(String firstName) {
 *     this.firstName = firstName;
 *   }
 *   
 *   public String getFirstName() {
 *     return firstName;
 *   }
 *   
 *   ...
 * }
 * </pre>
 * 
 * This will be mapped to:
 * <pre>
 * &lt;Address id='0' firstname='Joe' lastname='Black' street='World'/&gt;
 * </pre>
 * 
 * <strong>Usage</strong>
 * 
 * <pre>
 * Address address = BeanUtil.read(rtx, Address.class);
 * BeanUtil.write(wtx, address);
 * </pre>
 * 
 * <strong>Important</strong>
 * 
 * The following Java types are currentyl supported:
 * <li>boolean</li>
 * <li>int</li>
 * <li>long</li>
 * <li>float</li>
 * <li>double</li>
 * <li>String</li>
 * </p>
 */
public final class BeanUtil {

  /**
   * Hidden constructor.
   */
  private BeanUtil() {
    // Hidden.
  }

  /**
   * Read Java bean from TreeTank. Cursor of transaction is leaved where it was
   * before reading the Java bean.
   * 
   * @param <T> Java Bean.
   * @param rtx IReadTransaction to read.
   * @param clazz Class to create.
   * @return Instance of clazz with all properties set or null if there
   *         was no element or property stored.
   * @throws RuntimeException in case the class could not be instantiated
   *         or the property setter could not be invoked.
   */
  public static final <T> T read(
      final IReadTransaction rtx,
      final Class<T> clazz) {

    T target = null;

    try {

      // Check whether node points to element with clazz name.
      if (rtx.isElement()
          && rtx.getLocalPart().equalsIgnoreCase(clazz.getSimpleName())) {

        target = clazz.newInstance();

        // Loop over all children of node.
        for (int index = 0, length = rtx.getAttributeCount(); index < length; index++) {

          // Only fetch elements that contain a text.
          // Set (private) property.
          final Field field =
              clazz.getDeclaredField(rtx.getAttributeLocalPart(index));
          field.setAccessible(true);
          // Switch according to field type.
          switch (field.getType().getCanonicalName().hashCode()) {
          case -1374008726: // byte[]
            field.set(target, rtx.getAttributeValue(index));
            break;
          case 64711720: // boolean
            field.setBoolean(target, Boolean.parseBoolean(UTF.parseString(rtx
                .getAttributeValue(index))));
            break;
          case 104431: // int
            field.setInt(target, UTF.parseInt(rtx.getAttributeValue(index)));
            break;
          case 3327612: // long
            field.setLong(target, UTF.parseLong(rtx.getAttributeValue(index)));
            break;
          case 97526364: // float
            field.setFloat(target, Float.parseFloat(UTF.parseString(rtx
                .getAttributeValue(index))));
            break;
          case -1325958191: // double
            field.setDouble(target, Double.parseDouble(UTF.parseString(rtx
                .getAttributeValue(index))));
            break;
          case 1195259493: // String
            field.set(target, UTF.parseString(rtx.getAttributeValue(index)));
            break;
          default:
            throw new IllegalStateException(field.getType().getName());
          }
        }
      }

    } catch (Exception e) {
      // Transform into unchecked exception.
      throw new RuntimeException(e);
    }

    return target;
  }

  /**
   * Write Java bean to TreeTank. Cursors of transaction is moved to
   * "root" element node of newly written Java bean.
   * 
   * @param wtx IWriteTransaction to write.
   * @param object Java bean to write.
   * @return key of newly written Java bean element node.
   * @throws RuntimeException in case the object properties could not be
   *         read.
   */
  public static final long write(
      final IWriteTransaction wtx,
      final Object object) {

    long beanKey = IConstants.NULL_KEY;

    try {

      // Insert bean root element.
      beanKey =
          wtx.insertElementAsFirstChild(
              object.getClass().getSimpleName(),
              "",
              "");

      // Find all fields.
      final BeanInfo info =
          Introspector.getBeanInfo(object.getClass(), Object.class);
      for (final PropertyDescriptor property : info.getPropertyDescriptors()) {

        // Access private field.
        final Field field =
            object.getClass().getDeclaredField(property.getName());
        field.setAccessible(true);

        // Handle field ID annotation.
        if (field.getAnnotation(ID.class) != null) {
          if (field.getType() != long.class) {
            throw new IllegalStateException(
                "Only fields of type long are supported by the"
                    + " ID annotation.");
          }
          field.setLong(object, beanKey);
        }

        // Handle field FullText annotation.
        if (field.getAnnotation(FullText.class) != null) {
          if (field.getType() != String.class) {
            throw new IllegalStateException(
                "Only fields of type String are supported by the"
                    + " FullText annotation.");
          }
          final String string = (String) field.get(object);
          if (string != null) {
            wtx.index(string.toLowerCase(), beanKey);
            wtx.moveTo(beanKey);
          }
        }

        // Make sure not to serialize null fields.
        if (field.get(object) != null) {

          // Switch according to field type.
          byte[] bytes = null;
          switch (field.getType().getCanonicalName().hashCode()) {
          case -1374008726: // byte[]
            bytes = (byte[]) field.get(object);
            break;
          case 64711720: // boolean
            bytes = UTF.getBytes(field.get(object).toString());
            break;
          case 104431: // int
            bytes = UTF.getBytes(field.getInt(object));
            break;
          case 3327612: // long
            bytes = UTF.getBytes(field.getLong(object));
            break;
          case 97526364: // float
            bytes = UTF.getBytes(field.get(object).toString());
            break;
          case -1325958191: // double
            bytes = UTF.getBytes(field.get(object).toString());
            break;
          case 1195259493: // String
            bytes = UTF.getBytes((String) field.get(object));
            break;
          default:
            throw new IllegalStateException(field.getType().getName());
          }

          // Insert property as element with text.
          wtx.insertAttribute(property.getName(), "", "", bytes);

        }
      }

    } catch (Exception e) {
      // Transform into unchecked exception.
      throw new RuntimeException(e);
    }

    // Make sure the cursor selects the newly written Java bean element node.
    wtx.moveTo(beanKey);

    return beanKey;

  }

}
