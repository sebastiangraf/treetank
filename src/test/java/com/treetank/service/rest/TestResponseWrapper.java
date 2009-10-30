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

    public void addCookie(Cookie arg0) {
    }

    public void addDateHeader(String arg0, long arg1) {
    }

    public void addHeader(String arg0, String arg1) {

    }

    public void addIntHeader(String arg0, int arg1) {

    }

    public boolean containsHeader(String arg0) {
        return false;
    }

    public String encodeRedirectURL(String arg0) {
        return null;
    }

    public String encodeRedirectUrl(String arg0) {
        return null;
    }

    public String encodeURL(String arg0) {
        return null;
    }

    public String encodeUrl(String arg0) {
        return null;
    }

    public void sendError(int arg0) throws IOException {

    }

    public void sendError(int arg0, String arg1) throws IOException {

    }

    public void sendRedirect(String arg0) throws IOException {

    }

    public void setDateHeader(String arg0, long arg1) {

    }

    public void setHeader(String arg0, String arg1) {

    }

    public void setIntHeader(String arg0, int arg1) {

    }

    public void setStatus(int arg0) {

    }

    public void setStatus(int arg0, String arg1) {

    }

    public void flushBuffer() throws IOException {

    }

    public int getBufferSize() {
        return 0;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public String getContentType() {
        return null;
    }

    public Locale getLocale() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
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

    public PrintWriter getWriter() throws IOException {
        return null;
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {

    }

    public void resetBuffer() {

    }

    public void setBufferSize(int arg0) {

    }

    public void setCharacterEncoding(String arg0) {
        assertEquals(this.encoding, arg0);

    }

    public void setContentLength(int arg0) {

    }

    public void setContentType(String arg0) {
        assertEquals(this.contentType, arg0);

    }

    public void setLocale(Locale arg0) {

    }

    @Test
    public void testFake() {
        // Just to fool maven
    }

}
