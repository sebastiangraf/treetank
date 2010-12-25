package com.treetank.service.xml.xpath.expr;

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
