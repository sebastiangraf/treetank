/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.gui.view.sunburst;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.treetank.utils.LogWrapper;

/**
 * <h1>AbsController</h1>
 * 
 * <p>
 * Abstract controller, which provides methods to register and remove views/models as well as provides
 * methods to set and get properties from a registered model.
 * </p>
 * 
 * @param <M>
 *             The model which has to extend {@link AbsModel}.
 * @param <V>
 *             The view which has to extend {@link AbsView}.
 *             
 * @author Johannes Lichtenberger, University of Konstanz
 *  
 */
abstract class AbsController<M extends AbsModel, V extends AbsView> implements PropertyChangeListener {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER =
        new LogWrapper(LoggerFactory.getLogger(AbsController.class));
    
    /** Registered models. */
    private final List<M> mRegisteredModels;
    
    /** Registered views. */
    private final List<V> mRegisteredViews;

    /**
     * Constructor.
     */
    public AbsController() {
        mRegisteredModels = new LinkedList<M>();
        mRegisteredViews = new LinkedList<V>();
    }

    /**
     * Add a model.
     * 
     * @param paramModel
     *               The model to add.
     */
    public final void addModel(final M paramModel) {
        mRegisteredModels.add(paramModel);
        paramModel.addPropertyChangeListener(this);
    }

    /**
     * Remove a model.
     * 
     * @param paramModel
     *               The model to remove.
     */
    public final void removeModel(final M paramModel) {
        mRegisteredModels.remove(paramModel);
        paramModel.removePropertyChangeListener(this);
    }

    /**
     * Add a view.
     * 
     * @param paramView
     *               The view to add.
     */
    public final void addView(final V paramView) {
        mRegisteredViews.add(paramView);
    }

    /**
     * Remove a view. 
     * 
     * @param paramView
     *               The view to remove.
     */
    public final void removeView(final V paramView) {
        mRegisteredViews.remove(paramView);
    }
    
    /**
     * Get registered models.
     * 
     * @return The registeredModels.
     */
    public final List<M> getRegisteredModels() {
        return mRegisteredModels;
    }
    
    /**
     * Get registered views.
     * 
     * @return The registeredViews.
     */
    public final List<V> getRegisteredViews() {
        return mRegisteredViews;
    }

    /**
     * Observe property changes from registered models.
     * 
     * @param paramEvt
     *                 {@link PropertyChangeEvent}.
     */
    public final void propertyChange(final PropertyChangeEvent paramEvt) {
        for (final V view : mRegisteredViews) {
            view.modelPropertyChange(paramEvt);
        }
    }

    /**
     * This is a convenience method that subclasses can call upon
     * to fire property changes back to the models. This method
     * uses reflection to inspect each of the model classes
     * to determine whether it is the owner of the property
     * in question. If it isn't, a NoSuchMethodException is thrown,
     * which the method ignores.
     * 
     * @param paramPropertyName
     *            The name of the property.
     * @param paramNewValue
     *            An object that represents the new value
     *            of the property.
     */
    protected final void setModelProperty(final String paramPropertyName, final Object paramNewValue) {

        for (final M model : mRegisteredModels) {
            try {
                final Method method = model.getClass().getMethod("set" + paramPropertyName, new Class[] {
                    paramNewValue.getClass()
                });
                method.invoke(model, paramNewValue);
            } catch (final NoSuchMethodException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final IllegalArgumentException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final IllegalAccessException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final InvocationTargetException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 
     * @param paramProperty
     * @param paramNewValue
     */
    protected final void setModelProperty(final boolean paramProperty, final Object paramNewValue) {

        for (final M model : mRegisteredModels) {
            try {

                final Method method = model.getClass().getMethod("set" + paramProperty, new Class[] {
                    paramNewValue.getClass()
                });
                method.invoke(model, paramNewValue);

            } catch (final NoSuchMethodException e) {
                
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    final <T> T getModelProperty(final String paramPropertyName) {
        T retValue = null;
        
        for (final M model : mRegisteredModels) {
            try {
                final Method method = model.getClass().getMethod("get" + paramPropertyName);
                retValue = (T) method.invoke(model);
            } catch (final NoSuchMethodException e) {
                
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return retValue;
    }

}
