package com.treetank.service.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.Request;

public class HandledJettyServletRequest implements HandledHttpServletRequest {

    private final Request request;

    public HandledJettyServletRequest(final Request paramRequest) {
        this.request = paramRequest;
    }

    @Override
    public void setHandled(boolean handled) {
        request.setHandled(handled);
    }

    @Override
    public String getAuthType() {
        return request.getAuthType();
    }

    @Override
    public String getContextPath() {
        return request.getContextPath();
    }

    @Override
    public Cookie[] getCookies() {
        return request.getCookies();
    }

    @Override
    public long getDateHeader(String arg0) {
        return request.getDateHeader(arg0);
    }

    @Override
    public String getHeader(String arg0) {
        return request.getHeader(arg0);
    }

    @Override
    public Enumeration<?> getHeaderNames() {
        return request.getHeaderNames();
    }

    @Override
    public Enumeration<?> getHeaders(String arg0) {
        return request.getHeaders(arg0);
    }

    @Override
    public int getIntHeader(String arg0) {
        return request.getIntHeader(arg0);
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getPathInfo() {
        return request.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return request.getPathTranslated();
    }

    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    @Override
    public String getRequestURI() {
        return request.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return request.getRequestURL();
    }

    @Override
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    @Override
    public String getServletPath() {
        return request.getServletPath();
    }

    @Override
    public HttpSession getSession() {
        return request.getSession();
    }

    @Override
    public HttpSession getSession(boolean arg0) {
        return request.getSession(arg0);
    }

    @Override
    public Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return request.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return request.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return request.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    @Override
    public boolean isUserInRole(String arg0) {
        return request.isUserInRole(arg0);
    }

    @Override
    public Object getAttribute(String arg0) {
        return request.getAttribute(arg0);
    }

    @Override
    public Enumeration<?> getAttributeNames() {
        return request.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public String getLocalAddr() {
        return request.getLocalAddr();
    }

    @Override
    public String getLocalName() {
        return request.getLocalName();
    }

    @Override
    public int getLocalPort() {
        return request.getLocalPort();
    }

    @Override
    public Locale getLocale() {
        return request.getLocale();
    }

    @Override
    public Enumeration<?> getLocales() {
        return request.getLocales();
    }

    @Override
    public String getParameter(String arg0) {
        return request.getParameter(arg0);
    }

    @Override
    public Map<?, ?> getParameterMap() {
        return request.getParameterMap();
    }

    @Override
    public Enumeration<?> getParameterNames() {
        return request.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String arg0) {
        return request.getParameterValues(arg0);
    }

    @Override
    public String getProtocol() {
        return request.getProtocol();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return request.getReader();
    }

    @Override
    public String getRealPath(String arg0) {
        return request.getRealPath(arg0);
    }

    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    @Override
    public int getRemotePort() {
        return request.getRemotePort();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return request.getRequestDispatcher(arg0);
    }

    @Override
    public String getScheme() {
        return request.getScheme();
    }

    @Override
    public String getServerName() {
        return request.getServerName();
    }

    @Override
    public int getServerPort() {
        return request.getServerPort();
    }

    @Override
    public boolean isSecure() {
        return request.isSecure();
    }

    @Override
    public void removeAttribute(String arg0) {
        request.removeAttribute(arg0);
    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
        request.setAttribute(arg0, arg1);
    }

    @Override
    public void setCharacterEncoding(String arg0)
            throws UnsupportedEncodingException {
        request.setCharacterEncoding(arg0);
    }

}
