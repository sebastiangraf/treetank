/**
 * 
 */
package com.treetank.service.rest.helper;

import org.junit.Before;
import org.junit.Test;

import com.treetank.exception.TreetankRestException;
import com.treetank.service.rest.TestRequestWrapper;
import com.treetank.service.rest.TestResponseWrapper;
import com.treetank.utils.IConstants;

public class HelperCrossdomainTest {

    private HelperCrossdomain toTest;

    @Before
    public void setUp() {
        toTest = new HelperCrossdomain();
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.helper.HelperCrossdomain#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     * .
     */
    @Test
    public void testHandle() throws TreetankRestException {
        final TestRequestWrapper request = new TestRequestWrapper();
        request.setClassParam(true);
        final TestResponseWrapper response = new TestResponseWrapper();
        response.setClassParams(RESTConstants.CONTENT_TYPE.getContent(),
                IConstants.DEFAULT_ENCODING, RESTConstants.CROSSDOMAIN
                        .getContent().getBytes());
        toTest.handle(request, response);

    }
}
