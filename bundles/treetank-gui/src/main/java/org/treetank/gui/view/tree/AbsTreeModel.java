/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.gui.view.tree;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author Santhosh Kumar T
 */
public abstract class AbsTreeModel implements TreeModel {
    protected Object root;

    protected AbsTreeModel() {
    }

    protected AbsTreeModel(Object root) {
        this.root = root;
    }

    public void setRoot(Object root) {
        this.root = root;
        fireTreeStructureChanged(root, new Object[] {
            root
        }, null, null);
    }

    /*-------------------------------------------------[ Listeners ]---------------------------------------------------*/

    protected EventListenerList listenerList = new EventListenerList();

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        listenerList.add(TreeModelListener.class, listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        listenerList.remove(TreeModelListener.class, listener);
    }

    /*-------------------------------------------------[ Firing Changes ]---------------------------------------------------*/

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (e == null)
                e = new TreeModelEvent(this, path.getPath(), null, null);
            ((TreeModelListener)listeners[i + 1]).treeNodesChanged(e);
        }
    }

    public void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (e == null)
                e = new TreeModelEvent(source, path, childIndices, children);
            ((TreeModelListener)listeners[i + 1]).treeStructureChanged(e);
        }
    }

    public void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (e == null)
                e = new TreeModelEvent(source, path, childIndices, children);
            ((TreeModelListener)listeners[i + 1]).treeNodesInserted(e);
        }
    }

    public void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (e == null)
                e = new TreeModelEvent(source, path, childIndices, children);
            ((TreeModelListener)listeners[i + 1]).treeNodesRemoved(e);
        }
    }
}
