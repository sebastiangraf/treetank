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
 * Currently, only String properties are supported.
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
              field.set(target, UTF.convert(text.getValue()));
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

        // Insert property as element with text.
        if (isFirst) {
          wtx.insertElementAsFirstChild(property.getName(), "", "");
          wtx.insertTextAsFirstChild(UTF.convert(field.get(object).toString()));
          wtx.moveToParent();
          isFirst = false;
        } else {
          wtx.insertElementAsRightSibling(property.getName(), "", "");
          wtx.insertTextAsFirstChild(UTF.convert(field.get(object).toString()));
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
