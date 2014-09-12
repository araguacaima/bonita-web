/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.forms.server.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Haojie Yuan
 *
 */
public class ApplicationResourceCacheFilter implements Filter {

	protected FilterConfig filterConfig = null;

	protected HashMap expiresMap = new HashMap();

	/**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(ApplicationResourceCacheFilter.class.getName());

	/**
	 * resource : indicate the path of the resource
	 */
	public static final String RESOURCE_PATH_PARAM = "location";

	@Override
    public void init(final FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		expiresMap.clear();
		final Enumeration names = filterConfig.getInitParameterNames();
		while (names.hasMoreElements()) {
			try {
				final String name = (String) names.nextElement();
				final String value = filterConfig.getInitParameter(name);
				final Integer expire = Integer.valueOf(value);
				expiresMap.put(name, expire);
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error while init the ApplicationResourceCacheFilter in session");
				throw new ServletException(e);
			}
		}
	}

	@Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		final HttpServletResponse res = (HttpServletResponse) response;

		String ext = null;
		final String location = req.getParameter(RESOURCE_PATH_PARAM);
		if (location.lastIndexOf(".css") != -1) {
			final int dot = location.lastIndexOf(".css");
			ext = location.substring(dot + 1, dot + 4);
		} else if (location.lastIndexOf(".png") != -1) {
			final int dot = location.lastIndexOf(".png");
			ext = location.substring(dot + 1, dot + 4);
		} else if (location.lastIndexOf(".gif") != -1) {
			final int dot = location.lastIndexOf(".gif");
			ext = location.substring(dot + 1, dot + 4);
		} else if (location.lastIndexOf(".jpg") != -1) {
			final int dot = location.lastIndexOf(".jpg");
			ext = location.substring(dot + 1, dot + 4);
		} else if (location.lastIndexOf(".js") != -1) {
			final int dot = location.lastIndexOf(".js");
			ext = location.substring(dot + 1, dot + 3);
		}

		setResponseHeader(res, ext);
		chain.doFilter(req, res);
	}

	@Override
    public void destroy() {
		filterConfig = null;
	}

	private void setResponseHeader(final HttpServletResponse response, final String ext) {
		if (ext != null && ext.length() > 0) {
			final Integer expires = (Integer) expiresMap.get(ext);
			if (expires != null) {
				if (expires.intValue() > 0) {
					response.setHeader("Cache-Control", "max-age=" + expires.intValue()); // HTTP 1.1
				} else {
                    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
					response.setHeader("Pragma", "no-cache"); // HTTP 1.0
					response.setDateHeader("Expires", 0);
				}
			}
		}
	}

}
