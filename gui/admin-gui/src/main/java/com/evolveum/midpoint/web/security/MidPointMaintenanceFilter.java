/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.web.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.GenericFilterBean;

import com.evolveum.midpoint.api.logging.Trace;
import com.evolveum.midpoint.logging.TraceManager;

/**
 * 
 * @author lazyman
 * 
 */
public class MidPointMaintenanceFilter extends GenericFilterBean {

	static final String FILTER_APPLIED = "MidPointMaintenanceFilter_applied";
	private static final Trace TRACE = TraceManager.getTrace(MidPointMaintenanceFilter.class);
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	private String maintenanceUrl;
	private boolean maintenanceEnabled = false;
	private Set<String> ipList = new HashSet<String>();

	public void setMaintenanceUrl(String maintenanceUrl) {
		this.maintenanceUrl = maintenanceUrl;
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		
		if (request.getAttribute(FILTER_APPLIED) != null) {
			TRACE.debug("Maintenance filter already applied.");
			chain.doFilter(request, response);
			return;
		}
		request.setAttribute(FILTER_APPLIED, Boolean.TRUE);

//		System.out.println(request.getRequestURI());
//		System.out.println(request.getContextPath() + maintenanceUrl);
//		System.out.println(request.getHeader("referer") == null ? null : new URL(request.getHeader("referer")));
		boolean goingToMaintenancePage = request.getRequestURI().equals(request.getContextPath() + maintenanceUrl);
		
		String ipAddress = request.getRemoteAddr();
		if (maintenanceEnabled && !ipList.contains(ipAddress) && !goingToMaintenancePage) {
			TRACE.debug("Maintenance mode enabled, redirecting to '" + maintenanceUrl + "'");
			redirectStrategy.sendRedirect(request, (HttpServletResponse) response,
					maintenanceUrl);

			return;
		}
		
		TRACE.debug("chaining...");
		chain.doFilter(request, response);
	}
}
