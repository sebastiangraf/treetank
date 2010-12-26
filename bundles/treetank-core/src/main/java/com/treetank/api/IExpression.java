package com.treetank.api;

import com.treetank.exception.TTXPathException;

public interface IExpression {

    /**
     * Performs the expression dependent evaluation of the expression. (Template
     * method)
     * 
     * @throws TTXPathException
     *             if evaluation fails.
     */
    void evaluate() throws TTXPathException;

}
