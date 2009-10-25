package com.treetank.service.rest.helper;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.treetank.service.rest.TestRequestWrapper;
import com.treetank.service.rest.TestResponseWrapper;
import com.treetank.service.rest.TreeTankException;

public class HelperFaviconTest {

    private HelperFavicon toTest;

    @Before
    public void setUp() {
        toTest = new HelperFavicon();
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.helper.HelperFavicon#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     * .
     */
    @Test
    public void testHandle() {
        final TestRequestWrapper request = new TestRequestWrapper(true);
        final TestResponseWrapper response = new TestResponseWrapper("", "",
                null);
        try {
            toTest.handle(request, response);
        } catch (final TreeTankException e) {
            fail(e.toString());
        }
    }

}
