package com.treetank.service.rest;

import javax.servlet.http.HttpServletRequest;

public interface HandledHttpServletRequest extends HttpServletRequest {

    void setHandled(final boolean handled);

}
