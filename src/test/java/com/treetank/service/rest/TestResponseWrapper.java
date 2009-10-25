package com.treetank.service.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public final class TestResponseWrapper implements HttpServletResponse {

    private final String contentType;
    private final String encoding;
    private final List<Byte> buffer;
    private final byte[] content;

    public TestResponseWrapper(final String paramContentType,
            final String paramEncoding, final byte[] paramContent) {
        this.contentType = paramContentType;
        this.encoding = paramEncoding;
        buffer = new ArrayList<Byte>();
        content = paramContent;
    }

    public TestResponseWrapper() {
        this("", "", null);
    }

    @Override
    public void addCookie(Cookie arg0) {
    }

    @Override
    public void addDateHeader(String arg0, long arg1) {
    }

    @Override
    public void addHeader(String arg0, String arg1) {

    }

    @Override
    public void addIntHeader(String arg0, int arg1) {

    }

    @Override
    public boolean containsHeader(String arg0) {
        return false;
    }

    @Override
    public String encodeRedirectURL(String arg0) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String arg0) {
        return null;
    }

    @Override
    public String encodeURL(String arg0) {
        return null;
    }

    @Override
    public String encodeUrl(String arg0) {
        return null;
    }

    @Override
    public void sendError(int arg0) throws IOException {

    }

    @Override
    public void sendError(int arg0, String arg1) throws IOException {

    }

    @Override
    public void sendRedirect(String arg0) throws IOException {

    }

    @Override
    public void setDateHeader(String arg0, long arg1) {

    }

    @Override
    public void setHeader(String arg0, String arg1) {

    }

    @Override
    public void setIntHeader(String arg0, int arg1) {

    }

    @Override
    public void setStatus(int arg0) {

    }

    @Override
    public void setStatus(int arg0, String arg1) {

    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                buffer.add((byte) b);
            }
        };
    }

    public void checkWrittenBytes() {
        final byte[] newByte = new byte[this.buffer.size()];
        System.arraycopy(this.buffer.toArray(new Byte[this.buffer.size()]), 0,
                newByte, 0, newByte.length);
        assertEquals(newByte, content);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return null;
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public void setBufferSize(int arg0) {

    }

    @Override
    public void setCharacterEncoding(String arg0) {
        assertEquals(this.encoding, arg0);

    }

    @Override
    public void setContentLength(int arg0) {

    }

    @Override
    public void setContentType(String arg0) {
        assertEquals(this.contentType, arg0);

    }

    @Override
    public void setLocale(Locale arg0) {

    }

    @Test
    public void testFake() {
        // Just to fool maven
    }

}
