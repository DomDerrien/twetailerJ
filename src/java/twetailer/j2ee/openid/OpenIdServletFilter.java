package twetailer.j2ee.openid;

//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.util.ClassLoaderUtil;

/**
* A servlet filter that forwards users to the login page if they are not authenticated and
* redirects the user to their openid provider once they've submitted their openid identifier.
* The required web.xml configuration is the init-parameter "forwardUri".
*
* @author David Yu
* @created Jan 8, 2009
*/

public class OpenIdServletFilter implements Filter
{

  public static final String ERROR_MSG_ATTR = "openid_servlet_filter_msg";
  public static final String DEFAULT_ERROR_MSG = System.getProperty("openid.servlet_filter.default_error_msg", "Your openid could not be resolved.");
  public static final String ID_NOT_FOUND_MSG = System.getProperty("openid.servlet_filter.id_not_found_msg", "Your openid does not exist.");

  /**
   * The default forward uri handler that basically executes:
   * request.getRequestDispatcher(forwardUri).forward(request, response);
   */
  public static final ForwardUriHandler DEFAULT_FORWARD_URI_HANDLER = new ForwardUriHandler()
  {
      public void handle(String forwardUri, HttpServletRequest request,
              HttpServletResponse response) throws IOException, ServletException
      {
          request.getRequestDispatcher(forwardUri).forward(request, response);
      }
  };

  static final String SLASH = "/";

  protected String _forwardUri;
  protected RelyingParty _relyingParty;

  protected ForwardUriHandler _forwardHandler;

  /**
   * This method is called by the servlet container to configure this filter;
   * The init parameter "forwardUri" is required.
   * The relying party used will be the default {@link RelyingParty#getInstance() instance}
   * if it is not found in the servlet context attributes.
   */
  public void init(FilterConfig config) throws ServletException
  {
      _forwardUri = config.getInitParameter("forwardUri");
      if(_forwardUri==null)
          throw new ServletException("forwardUri must not be null.");

      // resolve from ServletContext
      _relyingParty = (RelyingParty)config.getServletContext().getAttribute(
              RelyingParty.class.getName());

      //default config if null
      if(_relyingParty==null)
          _relyingParty = RelyingParty.getInstance();

      String forwardUriHandlerParam = config.getInitParameter("forwardUriHandler");
      if(forwardUriHandlerParam!=null)
      {
          try
          {
              _forwardHandler = ClassLoaderUtil.newInstance(
                      forwardUriHandlerParam, OpenIdServletFilter.class);
          }
          catch (Exception e)
          {
              throw new ServletException(e);
          }
      }
      else
          _forwardHandler = DEFAULT_FORWARD_URI_HANDLER;
  }

  /**
   * Gets the configured forward uri.
   */
  public String getForwardUri()
  {
      return _forwardUri;
  }

  /**
   * Gets the configured relying party.
   */
  public RelyingParty getRelyingParty()
  {
      return _relyingParty;
  }

  /**
   * Delegates to the filter chain if the user associated with this request is authenticated.
   */
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
  throws IOException, ServletException
  {
      if(handle((HttpServletRequest)req, (HttpServletResponse)res))
          chain.doFilter(req, res);
  }

  public void destroy()
  {

  }

  /**
   * Returns true if the user associated with this request is authenticated.
   */
  public boolean handle(HttpServletRequest request, HttpServletResponse response)
  throws IOException, ServletException
  {
      return handle(request, response, _relyingParty, _forwardHandler, _forwardUri);
  }

  /**
   * Returns true if the user associated with this request is authenticated.
   */
  public static boolean handle(HttpServletRequest request, HttpServletResponse response,
          String forwardUri) throws IOException, ServletException
  {
      return handle(request, response, RelyingParty.getInstance(),
              DEFAULT_FORWARD_URI_HANDLER, forwardUri);
  }

  /**
   * Returns true if the user associated with this request is authenticated.
   */
  public static boolean handle(HttpServletRequest request, HttpServletResponse response,
          RelyingParty relyingParty, ForwardUriHandler forwardUriHandler, String forwardUri)
          throws IOException, ServletException
  {
      System.err.println("^^^^^^ JSessionID: " + request.getSession().getId());
      String errorMsg = DEFAULT_ERROR_MSG;
      try
      {
          OpenIdUser user = relyingParty.discover(request);
          if(user==null)
          {
              System.err.println("^^^^^^ OpenIdServletFilter: user.isDiscovered() == false");
              if(RelyingParty.isAuthResponse(request))
              {
                  // authentication timeout
                  System.err.println("^^^^^^ OpenIdServletFilter => redirect for timeout to: " + request.getRequestURI());
                  response.sendRedirect(request.getRequestURI());
              }
              else
              {
                  // set error msg if the openid_identifier is not resolved.
                  if(request.getParameter(relyingParty.getIdentifierParameter())!=null)
                      request.setAttribute(ERROR_MSG_ATTR, errorMsg);

                  // new user
                  System.err.println("^^^^^^ OpenIdServletFilter => forward because of new user: " + forwardUri);
                  forwardUriHandler.handle(forwardUri, request, response);
              }
              return false;
          }

          if(user.isAuthenticated())
          {
              // user already authenticated
              System.err.println("^^^^^^ OpenIdServletFilter: user.isAuthenticated() == true");
              return true;
          }

          if(user.isAssociated() && RelyingParty.isAuthResponse(request))
          {
              // verify authentication
              if(relyingParty.verifyAuth(user, request, response))
              {
                  // authenticated
                  // redirect to home to remove the query params
                  System.err.println("^^^^^^ OpenIdServletFilter => redirect to remove query params to: " + request.getRequestURI());
                  response.sendRedirect(request.getRequestURI());
              }
              else
              {
                  // failed verification
                  System.err.println("^^^^^^ OpenIdServletFilter => redirect because failed verification to: " + forwardUri);
                  forwardUriHandler.handle(forwardUri, request, response);
              }
              return false;
          }

          // associate and authenticate user
          StringBuffer url = request.getRequestURL();
          String trustRoot = url.substring(0, url.indexOf(SLASH, 9));
          String realm = url.substring(0, url.lastIndexOf(SLASH));
          String returnTo = url.toString();
          if(relyingParty.associateAndAuthenticate(user, request, response, trustRoot, realm,
                  returnTo))
          {
              // user is associated and then redirected to his openid provider for authentication
              return false;
          }
      }
      catch(UnknownHostException uhe)
      {
          errorMsg = ID_NOT_FOUND_MSG;
      }
      catch(FileNotFoundException fnfe)
      {
          errorMsg = ID_NOT_FOUND_MSG;
      }
      catch(Exception e)
      {
          e.printStackTrace();
          errorMsg = DEFAULT_ERROR_MSG;
      }
      request.setAttribute(ERROR_MSG_ATTR, errorMsg);
      System.err.println("^^^^^^ OpenIdServletFilter => forward with reason '" + errorMsg + "' to: " + forwardUri);
      forwardUriHandler.handle(forwardUri, request, response);
      return false;
  }

  /**
   * Pluggable handler to dispatch the request to a view/template.
   */
  public interface ForwardUriHandler
  {
      /**
       * Dispatches the request to a view/template.
       */
      public void handle(String forwardUri, HttpServletRequest request,
              HttpServletResponse response) throws IOException, ServletException;
  }

}
