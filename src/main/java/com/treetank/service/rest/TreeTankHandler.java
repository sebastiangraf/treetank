/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: TreeTankHandler.java 4471 2008-09-06 18:52:17Z kramis $
 */

package com.treetank.service.rest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.AbstractHandler;

public class TreeTankHandler extends AbstractHandler {

	private static final String PNG = ".png";

	private static final String JPEG = ".jpg";

	private static final String GIF = ".gif";

	private static final String STYLE = ".css";

	private static final String FLEX = ".swf";

	private static final String JAVASCRIPT = ".js";

	private static final String FAVICON = "/favicon.ico";

	private static final String CROSSDOMAIN = "/crossdomain.xml";

	private static final String POST = "POST";

	private static final String PUT = "PUT";

	private static final String DELETE = "DELETE";

	private static final String GET = "GET";

	private final HelperFavicon mHelperFavicon;

	private final HelperCrossdomain mHelperCrossdomain;

	private final HelperFile mHelperJavascript;

	private final HelperFile mHelperFlex;

	private final HelperFile mHelperStyle;

	private final HelperFile mHelperGif;

	private final HelperFile mHelperJpeg;

	private final HelperFile mHelperPng;

	private final HelperGet mHelperGet;

	private final HelperPost mHelperPost;

	private final HelperPut mHelperPut;

	private final HelperDelete mHelperDelete;

	public TreeTankHandler(final Map<String, TreeTankWrapper> map) {
		mHelperFavicon = new HelperFavicon();
		mHelperCrossdomain = new HelperCrossdomain();
		mHelperJavascript = new HelperFile("text/ecmascript");
		mHelperFlex = new HelperFile("application/x-shockwave-flash");
		mHelperStyle = new HelperFile("text/css");
		mHelperGif = new HelperFile("image/gif");
		mHelperJpeg = new HelperFile("image/jpeg");
		mHelperPng = new HelperFile("image/png");
		mHelperGet = new HelperGet(map);
		mHelperPost = new HelperPost(map);
		mHelperPut = new HelperPut(map);
		mHelperDelete = new HelperDelete(map);
	}

	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {

		try {

			if (request.getRequestURI().equalsIgnoreCase(FAVICON)) {
				mHelperFavicon.handle(request, response);
			} else if (request.getRequestURI().equalsIgnoreCase(CROSSDOMAIN)) {
				mHelperCrossdomain.handle(request, response);
			} else if (request.getRequestURI().endsWith(JAVASCRIPT)) {
				mHelperJavascript.handle(request, response);
			} else if (request.getRequestURI().endsWith(FLEX)) {
				mHelperFlex.handle(request, response);
			} else if (request.getRequestURI().endsWith(STYLE)) {
				mHelperStyle.handle(request, response);
			} else if (request.getRequestURI().endsWith(GIF)) {
				mHelperGif.handle(request, response);
			} else if (request.getRequestURI().endsWith(JPEG)) {
				mHelperJpeg.handle(request, response);
			} else if (request.getRequestURI().endsWith(PNG)) {
				mHelperPng.handle(request, response);
			} else if (request.getMethod().equalsIgnoreCase(GET)) {
				mHelperGet.handle(request, response);
			} else if (request.getMethod().equalsIgnoreCase(POST)) {
				if (request.getQueryString().equalsIgnoreCase(DELETE)) {
					mHelperDelete.handle(request, response);
				} else if (request.getQueryString().equalsIgnoreCase(POST)) {
					mHelperPost.handle(request, response);
				} else if (request.getQueryString().equalsIgnoreCase(PUT)) {
					mHelperPut.handle(request, response);
				} else {
					throw new TreeTankException(501, "Unknown operation.");
				}
			} else {
				throw new TreeTankException(501, "Unknown operation.");
			}

		} catch (TreeTankException te) {
			te.printStackTrace();
			response.sendError(te.getErrorCode(), te.getErrorMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}