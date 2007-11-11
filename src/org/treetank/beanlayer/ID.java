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

package org.treetank.beanlayer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <h1>ID</h1>
 * 
 * <p>
 * Field annotation to tell BeanUtil to use it as an ID field. The field
 * must be <code>long</code> and it is mapped to the node key.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ID {

}
