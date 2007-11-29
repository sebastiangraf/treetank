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
 * $Id$
 */

package org.treetank.beanlayer;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.TypedValue;

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
 *   // Bean fields.
 *   &#64;ID
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
 * BeanUtil.remove(wtx, address);
 * </pre>
 * 
 * <strong>Important</strong>
 * 
 * The following Java types are currently supported:
 * <ul>
 * <li>boolean</li>
 * <li>int</li>
 * <li>long</li>
 * <li>float</li>
 * <li>double</li>
 * <li>String</li>
 * <li>byte[]</li>
 * </ul>
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
      if (rtx.isElementKind()
          && rtx.getName().equalsIgnoreCase(clazz.getSimpleName())) {

        target = clazz.newInstance();

        // Loop over all children of node.
        for (int index = 0, length = rtx.getAttributeCount(); index < length; index++) {

          // Only fetch elements that contain a text.
          // Set (private) property.
          final Field field =
              clazz.getDeclaredField(rtx.getAttributeName(index));
          field.setAccessible(true);
          // Switch according to field type.
          switch (field.getType().getCanonicalName().hashCode()) {
          case -1374008726: // byte[]
            if (rtx.getAttributeValueType(index) != IReadTransaction.BYTEARRAY_TYPE) {
              throw new IllegalStateException(
                  "Type mismatch: should be byte[].");
            }
            field.set(target, rtx.getAttributeValueAsByteArray(index));
            break;
          case 64711720: // boolean
            if (rtx.getAttributeValueType(index) != IReadTransaction.BOOLEAN_TYPE) {
              throw new IllegalStateException(
                  "Type mismatch: should be boolean.");
            }
            field.setBoolean(target, rtx.getAttributeValueAsBoolean(index));
            break;
          case 104431: // int
            if (rtx.getAttributeValueType(index) != IReadTransaction.INT_TYPE) {
              throw new IllegalStateException("Type mismatch: should be int.");
            }
            field.setInt(target, rtx.getAttributeValueAsInt(index));
            break;
          case 3327612: // long
            if (rtx.getAttributeValueType(index) != IReadTransaction.LONG_TYPE) {
              throw new IllegalStateException("Type mismatch: should be long.");
            }
            field.setLong(target, rtx.getAttributeValueAsLong(index));
            break;
          case 97526364: // float
            field.setFloat(target, Float.parseFloat(TypedValue.parseString(rtx
                .getAttributeValueAsByteArray(index))));
            break;
          case -1325958191: // double
            field.setDouble(target, Double.parseDouble(TypedValue
                .parseString(rtx.getAttributeValueAsByteArray(index))));
            break;
          case 1195259493: // String
            if (rtx.getAttributeValueType(index) != IReadTransaction.STRING_TYPE) {
              throw new IllegalStateException(
                  "Type mismatch: should be string.");
            }
            field.set(target, rtx.getAttributeValueAsString(index));
            break;
          default:
            throw new IllegalStateException(field.getType().getName());
          }
        }
      } else {
        throw new IllegalStateException("Selected node does not contain a '"
            + clazz.getSimpleName()
            + "'");
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

    long beanKey = IReadTransaction.NULL_NODE_KEY;

    try {

      // Insert bean root element.
      beanKey =
          wtx.insertElementAsFirstChild(object.getClass().getSimpleName(), "");

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
            wtx.insertToken(string, beanKey);
            wtx.moveTo(beanKey);
          }
        }

        // Make sure not to serialize null fields.
        if (field.get(object) != null) {

          // Switch according to field type.
          byte[] bytes = null;
          int type = IReadTransaction.BYTEARRAY_TYPE;
          switch (field.getType().getCanonicalName().hashCode()) {
          case -1374008726: // byte[]
            bytes = (byte[]) field.get(object);
            type = IReadTransaction.BYTEARRAY_TYPE;
            break;
          case 64711720: // boolean
            bytes = TypedValue.getBytes(field.getBoolean(object));
            type = IReadTransaction.BOOLEAN_TYPE;
            break;
          case 104431: // int
            bytes = TypedValue.getBytes(field.getInt(object));
            type = IReadTransaction.INT_TYPE;
            break;
          case 3327612: // long
            bytes = TypedValue.getBytes(field.getLong(object));
            type = IReadTransaction.LONG_TYPE;
            break;
          case 97526364: // float
            bytes = TypedValue.getBytes(field.get(object).toString());
            type = IReadTransaction.BYTEARRAY_TYPE;
            break;
          case -1325958191: // double
            bytes = TypedValue.getBytes(field.get(object).toString());
            type = IReadTransaction.BYTEARRAY_TYPE;
            break;
          case 1195259493: // String
            bytes = TypedValue.getBytes((String) field.get(object));
            type = IReadTransaction.STRING_TYPE;
            break;
          default:
            throw new IllegalStateException(field.getType().getName());
          }

          // Insert property as element with text.
          wtx.insertAttribute(property.getName(), "", type, bytes);

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

  /**
   * Remove Java bean from TreeTank. Cursor of transaction is leaved according
   * to the semantics of <code>IWriteTransaction.remove()</code>. All contained
   * full text fields are removed from the full text index.
   * 
   * @param wtx IWriteTransaction to write to.
   * @param object Object to remove.
   * @throws RuntimeException in case the class could not be instantiated
   *         or the property setter could not be invoked.
   */
  public static final void remove(
      final IWriteTransaction wtx,
      final Object object) {
    // Check whether node points to element with clazz name.
    if (wtx.isElementKind()
        && wtx.getName().equalsIgnoreCase(object.getClass().getSimpleName())) {

      final long nodeKey = wtx.getNodeKey();

      // Remove subtree.
      wtx.remove();

      // Remove all fields with FullText annotation.
      try {
        final BeanInfo info =
            Introspector.getBeanInfo(object.getClass(), Object.class);
        for (final PropertyDescriptor property : info.getPropertyDescriptors()) {

          // Access private field.
          final Field field =
              object.getClass().getDeclaredField(property.getName());
          field.setAccessible(true);

          // Handle field ID annotation.
          if (field.getAnnotation(FullText.class) != null) {
            wtx.removeToken((String) field.get(object), nodeKey);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new IllegalStateException("Selected node does not contain a '"
          + object.getClass().getSimpleName()
          + "'");
    }
  }

}
