/**
 * 
 */
package com.treetank.service.rest.helper;

import static org.junit.Assert.fail;

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
    public void testHandle() {
        final TestRequestWrapper request = new TestRequestWrapper();
        request.setClassParam(true);
        final TestResponseWrapper response = new TestResponseWrapper();
        response.setClassParams(HelperCrossdomain.CONTENT_TYPE,
                IConstants.DEFAULT_ENCODING, HelperCrossdomain.CROSSDOMAIN);
        try {
            toTest.handle(request, response);
        } catch (final TreetankRestException e) {
            fail(e.toString());
        }

    }
}
