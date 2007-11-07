/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: INode.java 3277 2007-10-25 19:30:30Z kramis $
 */

package org.treetank.xmllayer;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import org.treetank.api.FullText;
import org.treetank.api.INode;
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
 *   private String firstName;
 *   private String lastName;
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
 * &lt;address&gt;
 *   &lt;firstname&gt;Joe&lt;/firstname&gt;
 *   &lt;lastname&gt;Black&lt;/lastname&gt;
 *   &lt;street&gt;World&lt;/street&gt;
 * &lt;/address&gt;
 * </pre>
 * 
 * <strong>Usage</strong>
 * 
 * <pre>
 * Address address = BeanUtil.read(rtx, parentElement, Address.class);
 * BeanUtil.write(wtx, parentElement, address);
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
   * Read Java bean from TreeTank.
   * 
   * @param rtx IReadTransaction to read.
   * @param clazz Class to create.
   * @return Instance of clazz with all properties set or null if there
   *         was no element or property stored.
   * @throws RuntimeException in case the class could not be instantiated
   *         or the property setter could not be invoked.
   */
  public static final Object read(
      final IReadTransaction rtx,
      final Class<? extends Object> clazz) {

    Object target = null;

    try {

      // Check whether node points to element with clazz name.
      if (rtx.isElement()
          && rtx.getLocalPart().equalsIgnoreCase(clazz.getName())) {
        target = clazz.newInstance();

        // Loop over all children of node.
        INode node = rtx.moveToFirstChild();
        while (node != null) {

          // Only fetch elements that contain a text.
          if (node.isElement() && node.hasFirstChild()) {
            final INode text = node.getFirstChild(rtx);
            if (text.isText()) {
              // Set (private) property.
              final Field field =
                  clazz.getDeclaredField(node.getLocalPart(rtx));
              field.setAccessible(true);
              // Switch according to field type.
              switch (field.getType().getName().hashCode()) {
              case 64711720: // boolean
                field.setBoolean(target, Boolean.parseBoolean(UTF
                    .parseString(text.getValue())));
                break;
              case 104431: // int
                field.setInt(target, UTF.parseInt(text.getValue()));
                break;
              case 3327612: // long
                field.setLong(target, UTF.parseLong(text.getValue()));
                break;
              case 97526364: // float
                field.setFloat(target, Float.parseFloat(UTF.parseString(text
                    .getValue())));
                break;
              case -1325958191: // double
                field.setDouble(target, Double.parseDouble(UTF.parseString(text
                    .getValue())));
                break;
              case 1195259493: // String
                field.set(target, UTF.parseString(text.getValue()));
                break;
              default:
                throw new IllegalStateException(field.getType().getName());
              }
            }
          }

          node = node.getRightSibling(rtx);
        }

      }

    } catch (Exception e) {
      // Transform into unchecked exception.
      throw new RuntimeException(e);
    }

    return target;
  }

  /**
   * Write Java bean to TreeTank.
   * 
   * @param wtx IWriteTransaction to write.
   * @param object Java bean to write.
   * @throws RuntimeException in case the object properties could not be
   *         read.
   */
  public static final void write(
      final IWriteTransaction wtx,
      final Object object) {

    try {

      // Insert bean root element.
      final long key =
          wtx.insertElementAsFirstChild(object.getClass().getName(), "", "");

      // Find all properties.
      boolean isFirst = true;
      final BeanInfo info =
          Introspector.getBeanInfo(object.getClass(), Object.class);
      for (final PropertyDescriptor property : info.getPropertyDescriptors()) {

        // Get field value.
        final Field field =
            object.getClass().getDeclaredField(property.getName());
        field.setAccessible(true);

        // Switch according to field type.
        byte[] bytes = null;
        switch (field.getType().getName().hashCode()) {
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
          bytes = UTF.getBytes(field.get(object).toString());
          break;
        default:
          throw new IllegalStateException(field.getType().getName());
        }

        // Insert property as element with text.
        if (isFirst) {
          wtx.insertElementAsFirstChild(property.getName(), "", "");
          wtx.insertTextAsFirstChild(bytes);
          wtx.moveToParent();
          isFirst = false;
        } else {
          wtx.insertElementAsRightSibling(property.getName(), "", "");
          wtx.insertTextAsFirstChild(bytes);
          wtx.moveToParent();
        }
      }

      // Find all full text index annotated fields.
      for (final Field field : object.getClass().getDeclaredFields()) {
        if (field.getAnnotation(FullText.class) != null) {
          field.setAccessible(true);
          wtx.index(field.get(object).toString().toLowerCase(), key);
        }
      }

    } catch (Exception e) {
      // Transform into unchecked exception.
      throw new RuntimeException(e);
    }

  }

}
