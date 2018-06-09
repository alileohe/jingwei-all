package com.taobao.jingwei.webconsole.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.common.lang.StringUtil;
import com.taobao.ops.security.client.LoginCheck;
import com.taobao.opssecurity.common.exception.OpsSecurityException;
import com.taobao.opssecurity.common.security.CookieCrypt;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 10, 2013 2:08:41 PM
 *
 * @desc 
 */
public class JingweiSecurityFilter implements Filter {
	private static Log log = LogFactory.getLog(JingweiSecurityFilter.class);

	private static final String LOGIN_URL = "https://login.alibaba-inc.com/ssoLogin.htm";

	// 把用户名缓存在用户浏览器，下次就不用依赖ops的东东了
	private static final String JINGWEI_COOKEIS_KEY = "JINGWEI_COOKEIS_KEY";

	// 放到session中，随着request逐流
	public static final String NICK_NAME_PARAM = "NICK_NAME";

	@Autowired


	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		// 查看jingwei-cookei中有没有这个key
		String userName = checkCookie((HttpServletRequest) request, JINGWEI_COOKEIS_KEY);

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		if (StringUtil.isBlank(userName)) {

			// 从ops获取花名
			try {
				LoginCheck loginCheck = new LoginCheck();
				userName = loginCheck.checkLoginState(req, res);

				if (StringUtil.isBlank(userName)) {
					//跳转到同一登陆页面,现在什么也不做
					//res.sendRedirect(LOGIN_URL);
					//return;
				} else {
					// 把花名放到jingwei-cookie中
					String encrypt = CookieCrypt.cookieEncrypt(userName);

					Cookie cookie = new Cookie(JINGWEI_COOKEIS_KEY, encrypt);
					res.addCookie(cookie);

					log.warn("current login user is : " + userName);
				}

			} catch (OpsSecurityException e) {
				log.error("get uinify login user cookie failed. ", e);
			} catch (Exception e) {
				log.error("encrypt user nickname failed. ", e);
				//res.sendRedirect(LOGIN_URL);
				//return;
			}
		}

		// 获取到用户花名
		if (StringUtil.isNotBlank(userName)) {
			req.setAttribute(NICK_NAME_PARAM, userName);
		}

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {

	}

	/**
	 * 
	 * @param request
	 * @param key
	 * @return <code>null</code> 表示cookie中 指定的key不存在、值不存在、
	 */
	public String checkCookie(HttpServletRequest request, String key) {
		Cookie cookies[] = request.getCookies();

		if ((cookies == null) || (cookies.length == 0)) {
			// 没有cookies,说明还没有登录
			return null;
		} else {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];

				// 已经有了指定的cookie了
				if (key.equalsIgnoreCase(cookie.getName())) {

					// 这里一个合法的cookie
					if (null != cookie.getValue() && !"".equalsIgnoreCase(cookie.getValue())) {
						String value_code = cookie.getValue();
						String value_real = null;
						try {
							value_real = CookieCrypt.cookieDecrypt(value_code);
						} catch (Exception e) {
							log.error("", e);
						}
						return ("".equalsIgnoreCase(value_real) || null == value_real) ? null : value_real;
					}
				}
			}
		}
		return null;
	}

}
