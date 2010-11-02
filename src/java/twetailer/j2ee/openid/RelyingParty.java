package twetailer.j2ee.openid;

//========================================================================
//Copyright 2007-2008 David Yu dyuproject@gmail.com
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dyuproject.openid.Association;
import com.dyuproject.openid.AuthRedirection;
import com.dyuproject.openid.Constants;
import com.dyuproject.openid.DefaultDiscovery;
import com.dyuproject.openid.DiffieHellmanAssociation;
import com.dyuproject.openid.Discovery;
import com.dyuproject.openid.Identifier;
import com.dyuproject.openid.IdentifierSelectUserCache;
import com.dyuproject.openid.OpenIdContext;
import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.OpenIdUserManager;
import com.dyuproject.openid.SimpleRedirection;
import com.dyuproject.openid.YadisDiscovery;
import com.dyuproject.openid.Discovery.UserCache;
import com.dyuproject.openid.Identifier.Resolver;
import com.dyuproject.openid.Identifier.ResolverCollection;
import com.dyuproject.openid.manager.HttpSessionUserManager;
import com.dyuproject.util.ClassLoaderUtil;
import com.dyuproject.util.http.HttpConnector;
import com.dyuproject.util.http.SimpleHttpConnector;
import com.dyuproject.util.http.UrlEncodedParameterMap;

/**
* Relying party which discovers, associates and verifies the authentication of a user.
* An implementation of RelyingParty.Listener will enable you to listen to events
* during a user's authentication lifecycle.
*
* <blockquote>
* <pre>
*   OpenIdUser user = _relyingParty.discover(request);
*   if(user==null)
*   {
*       if(RelyingParty.isAuthResponse(request))
*       {
*           // authentication timeout
*           response.sendRedirect(request.getRequestURI());
*       }
*       else
*       {
*           // set error msg if the openid_identifier is not resolved.
*           if(request.getParameter(_relyingParty.getIdentifierParameter())!=null)
*               request.setAttribute(OpenIdServletFilter.ERROR_MSG_ATTR, errorMsg);
*
*           // new user
*           request.getRequestDispatcher("/login.jsp").forward(request, response);
*       }
*       return;
*   }
*
*   if(user.isAuthenticated())
*   {
*       // user already authenticated
*       request.getRequestDispatcher("/home.jsp").forward(request, response);
*       return;
*   }
*
*   if(user.isAssociated() && RelyingParty.isAuthResponse(request))
*   {
*       // verify authentication
*       if(_relyingParty.verifyAuth(user, request, response))
*       {
*           // authenticated
*          // redirect to home to remove the query params instead of doing:
*           // request.setAttribute("user", user); request.getRequestDispatcher("/home.jsp").forward(request, response);
*           response.sendRedirect(request.getContextPath() + "/home/");
*       }
*       else
*       {
*           // failed verification
*           request.getRequestDispatcher("/login.jsp").forward(request, response);
*       }
*       return;
*   }
*
*   StringBuffer url = request.getRequestURL();
*   String trustRoot = url.substring(0, url.indexOf("/", 9));
*   String realm = url.substring(0, url.lastIndexOf("/"));
*   String returnTo = url.toString();
*   if(_relyingParty.associateAndAuthenticate(user, request, response, trustRoot, realm, returnTo))
*   {
*       // successful association
*       return;
*   }
* </pre>
* </blockquote>
*
* @author David Yu
* @created Sep 21, 2008
*/

public final class RelyingParty
{

  /**
   * The default resource path ("openid.properties" from classpath).
   */
  public static final String DEFAULT_RESOURCE_PATH = "openid.properties";
  /**
   * "openid_identifier"
   */
  public static final String DEFAULT_IDENTIFIER_PARAMETER = "openid_identifier";
  static final boolean DEFAULT_AUTOMATIC_REDIRECT = true;

  private static RelyingParty __instance = null;

  /**
   * Gets the default instance configured from the properties file found in the
   * {@link #DEFAULT_RESOURCE_PATH default resource path}.
   * If its not found, a new instance will be created (using the default constructor of
   * the RelyingParty) and set as the default instance.
   */
  public static RelyingParty getInstance()
  {
      RelyingParty instance = __instance;
      if(instance==null)
      {
          synchronized(RelyingParty.class)
          {
              instance = __instance;
              if(instance==null)
              {
                  URL resource = getResource(DEFAULT_RESOURCE_PATH);
                  if(resource==null)
                  {
                      // default setting
                      __instance = instance = new RelyingParty();
                  }
                  else
                  {
                      try
                      {
                          __instance = instance = newInstance(resource);
                      }
                      catch(IOException e)
                      {
                          throw new RuntimeException(e);
                      }
                  }
              }
          }
      }
      return instance;
  }

  /**
   * Creates a new instance from the specified {@code resourceLoc} in the classpath, which
   * will be parsed/loaded to a {@link Properties}.
   */
  public static RelyingParty newInstance(String resourceLoc)
  {
      URL resource = getResource(resourceLoc);
      if(resource==null)
          throw new RuntimeException(resourceLoc + " not found in the classpath.");
      try
      {
          return newInstance(resource);
      }
      catch(IOException e)
      {
          throw new RuntimeException(e);
      }
  }

  /**
   * Creates a new instance from the specified URL {@code resource}, which will be
   * parsed/loaded to a {@link Properties}.
   */
  public static RelyingParty newInstance(URL resource) throws IOException
  {
      return newInstance(resource.openStream());
  }

  /**
   * Creates a new instance from the specified InputStream {@code in}, which will be
   * parsed/loaded to a {@link Properties}.
   */
  public static RelyingParty newInstance(InputStream in) throws IOException
  {
      Properties props = new Properties();
      props.load(in);
      return newInstance(props);
  }

  /**
   * Creates a new instance configured from the given {@code properties}.
   */
  public static RelyingParty newInstance(Properties properties)
  {
      // discovery
      String discoveryParam = properties.getProperty("openid.discovery");
      Discovery discovery = discoveryParam==null ? new DefaultDiscovery() :
          (Discovery)newObjectInstance(discoveryParam);

      // association
      String associationParam = properties.getProperty("openid.association");
      Association association = associationParam==null ? new DiffieHellmanAssociation() :
          (Association)newObjectInstance(associationParam);

      // http connector
      String httpConnectorParam = properties.getProperty("openid.httpconnector");
      HttpConnector httpConnector = httpConnectorParam==null ? new SimpleHttpConnector() :
          (HttpConnector)newObjectInstance(httpConnectorParam);

      // user manager
      String managerParam = properties.getProperty("openid.user.manager");
      OpenIdUserManager manager = managerParam == null ? new HttpSessionUserManager() :
          (OpenIdUserManager)newObjectInstance(managerParam);
      manager.init(properties);

      // openid user cache
      String userCacheParam = properties.getProperty("openid.user.cache");
      UserCache userCache = userCacheParam==null ? new IdentifierSelectUserCache() :
          (UserCache)newObjectInstance(userCacheParam);

      // openid automatic redirect
      // when the user is redirected to his provider and he somehow navigates away from his
      // provider and returns to your site ... the relying party will do an automatic redirect
      // back to his provider for authentication (if set to true)
      String automaticRedirectParam = properties.getProperty("openid.automatic_redirect");
      boolean automaticRedirect = automaticRedirectParam==null ? DEFAULT_AUTOMATIC_REDIRECT :
          Boolean.parseBoolean(automaticRedirectParam);

      // auth redirection
      String authRedirectionParam = properties.getProperty("openid.authredirection");
      AuthRedirection authRedirection = authRedirectionParam==null ? new SimpleRedirection() :
          (AuthRedirection)newObjectInstance(authRedirectionParam);

      // identifier parameter (default is openid_identifier)
      String identifierParameter = properties.getProperty("openid.identifier.parameter",
              DEFAULT_IDENTIFIER_PARAMETER);

      String identifierAsServerParam = properties.getProperty("openid.identifier_as_server");
      boolean identifierAsServer = "true".equals(identifierAsServerParam);

      RelyingParty relyingParty = new RelyingParty(
              new OpenIdContext(discovery, association, httpConnector),
              manager, userCache, automaticRedirect, identifierAsServer,
              authRedirection, identifierParameter);

      // relying party listeners
      String listenersParam = properties.getProperty("openid.relyingparty.listeners");
      if(listenersParam!=null)
      {
          StringTokenizer tokenizer = new StringTokenizer(listenersParam, ",;");
          while(tokenizer.hasMoreTokens())
              relyingParty.addListener((Listener)newObjectInstance(tokenizer.nextToken().trim()));
      }

      // openid identifier resolvers
      String resolversParam = properties.getProperty("openid.identifier.resolvers");
      if(resolversParam!=null)
      {
          StringTokenizer tokenizer = new StringTokenizer(resolversParam, ",;");
          while(tokenizer.hasMoreTokens())
              relyingParty.addResolver((Resolver)newObjectInstance(tokenizer.nextToken().trim()));
      }

      return relyingParty;
  }

  static URL getResource(String resource)
  {
      return ClassLoaderUtil.getResource(resource, RelyingParty.class);
  }

  static Object newObjectInstance(String className)
  {
      try
      {
          return ClassLoaderUtil.newInstance(className, RelyingParty.class);
      }
      catch(Exception e)
      {
          throw new RuntimeException(e);
      }
  }

  /**
   * Copies all the request parameters into a Map<String,String> object.
   */
  @SuppressWarnings("unchecked")
  public static Map<String,String> getAuthParameters(HttpServletRequest request)
  {
      Map<String,String> params = new HashMap<String,String>();
      Enumeration<String> names = (Enumeration<String>)request.getParameterNames();
      while(names.hasMoreElements())
      {
          String name = names.nextElement();
          params.put(name, request.getParameter(name));
      }
      return params;
  }

  /**
   * Returns true if we have a positive response from the OpenID provider.
   * If the user {@link OpenIdUser#isAssociated() is associated}
   * and we have an auth response, then we can
   * {@link #verifyAuth(OpenIdUser, HttpServletRequest, HttpServletResponse) verify} the user.
   */
  public static boolean isAuthResponse(HttpServletRequest request)
  {
      return Constants.Mode.ID_RES.equals(request.getParameter(Constants.OPENID_MODE));
  }

  /**
   * Returns true if the user has cancelled the authentication on his openid provider.
   */
  public static boolean isAuthCancel(HttpServletRequest request)
  {
      return Constants.Mode.CANCEL.equals(request.getParameter(Constants.OPENID_MODE));
  }

  /**
   * Gets the UrlEncodedParameterMap filled with the openid parameters that is used to
   * redirect the user to his openid provider.
   */
  public static UrlEncodedParameterMap getAuthUrlMap(OpenIdUser user, String trustRoot,
          String realm, String returnTo)
  {
      if(!user.isAssociated())
          throw new IllegalArgumentException("claimed_id of user has not been verified.");

      UrlEncodedParameterMap map = new UrlEncodedParameterMap(user.getOpenIdServer());

      String identity = user.getOpenIdDelegate();
      if(identity==null)
          identity = user.getClaimedId();

      map.put(Constants.OPENID_NS, Constants.DEFAULT_NS);
      map.put(Constants.OPENID_MODE, Constants.Mode.CHECKID_SETUP);

      map.put(Constants.OPENID_TRUST_ROOT, trustRoot);
      map.put(Constants.OPENID_REALM, realm);
      map.put(Constants.OPENID_RETURN_TO, returnTo);
      map.put(Constants.OPENID_ASSOC_HANDLE, user.getAssocHandle());

      map.put(Constants.OPENID_IDENTITY, identity);
      map.put(Constants.OPENID_CLAIMED_ID, identity);

      return map;
  }

  /**
   * Gets the StringBuilder filled with the openid parameters that is used to
   * redirect the user to his openid provider.
   */
  public static StringBuilder getAuthUrlBuffer(OpenIdUser user, String trustRoot, String realm,
          String returnTo)
  {
      if(!user.isAssociated())
          throw new IllegalArgumentException("claimed_id of user has not been verified.");

      String identity = user.getOpenIdDelegate();
      if(identity==null)
          identity = user.getClaimedId();

      StringBuilder buffer = new StringBuilder().append(user.getOpenIdServer());
      char separator = user.getOpenIdServer().indexOf('?')==-1 ? '?' : '&';
      buffer.append(separator).append(Constants.OPENID_NS).append('=').append(Constants.DEFAULT_NS);

      buffer.append('&').append(Constants.OPENID_MODE).append('=').append(
              Constants.Mode.CHECKID_SETUP);

      buffer.append('&').append(Constants.OPENID_TRUST_ROOT).append('=').append(
              UrlEncodedParameterMap.encode(trustRoot));
      buffer.append('&').append(Constants.OPENID_REALM).append('=').append(
              UrlEncodedParameterMap.encode(realm));
      buffer.append('&').append(Constants.OPENID_RETURN_TO).append('=').append(
              UrlEncodedParameterMap.encode(returnTo));
      buffer.append('&').append(Constants.OPENID_ASSOC_HANDLE).append('=').append(
              UrlEncodedParameterMap.encode(user.getAssocHandle()));

      buffer.append('&').append(Constants.OPENID_IDENTITY).append('=').append(
              UrlEncodedParameterMap.encode(identity));
      buffer.append('&').append(Constants.OPENID_CLAIMED_ID).append('=').append(
              UrlEncodedParameterMap.encode(identity));

      return buffer;
  }

  /**
   * Gets the string url with the openid parameters that is used to
   * redirect the user to his openid provider.
   */
  public static String getAuthUrlString(OpenIdUser user, String trustRoot, String realm,
          String returnTo)
  {
      return getAuthUrlBuffer(user, trustRoot, realm, returnTo).toString();
  }

  private final OpenIdContext _context;
  private final OpenIdUserManager _manager;
  private final UserCache _userCache;
  private final boolean _automaticRedirect;
  private final boolean _identifierAsServer;
  private final AuthRedirection _authRedirection;
  private final String _identifierParameter;
  private final ListenerCollection _listener = new ListenerCollection();
  private final ResolverCollection _resolver = new ResolverCollection();

  public RelyingParty()
  {
      this(DEFAULT_AUTOMATIC_REDIRECT);
  }

  public RelyingParty(boolean automaticRedirect)
  {
      this(new OpenIdContext(new DefaultDiscovery(), new DiffieHellmanAssociation(),
              new SimpleHttpConnector()), new HttpSessionUserManager(), automaticRedirect);
  }

  public RelyingParty(OpenIdContext context, OpenIdUserManager manager)
  {
      this(context, manager, DEFAULT_AUTOMATIC_REDIRECT);
  }

  public RelyingParty(OpenIdUserManager manager, UserCache userCache)
  {
      this(new OpenIdContext(new DefaultDiscovery(), new DiffieHellmanAssociation(),
              new SimpleHttpConnector()), manager, DEFAULT_AUTOMATIC_REDIRECT);
  }

  public RelyingParty(OpenIdContext context, OpenIdUserManager manager, boolean automaticRedirect)
  {
      this(context, manager, new IdentifierSelectUserCache(), automaticRedirect);
  }

  public RelyingParty(OpenIdContext context, OpenIdUserManager manager, UserCache userCache,
          boolean automaticRedirect)
  {
      this(context, manager, userCache, automaticRedirect, false, new SimpleRedirection(),
              DEFAULT_IDENTIFIER_PARAMETER);
  }

  public RelyingParty(OpenIdContext context, OpenIdUserManager manager, UserCache userCache,
          boolean automaticRedirect, boolean identifierAsServer,
          AuthRedirection authRedirection, String identifierParameter)
  {
      _context = context;
      _manager = manager;
      _userCache = userCache;
      _automaticRedirect = automaticRedirect;
      _identifierAsServer = identifierAsServer;
      _authRedirection = authRedirection;
      _identifierParameter = identifierParameter;
  }

  /**
   * Gets the {@link OpenIdUser} {@link OpenIdUserManager manager}.
   */
  public OpenIdUserManager getOpenIdUserManager()
  {
      return _manager;
  }

  /**
   * Gets the {@link OpenIdContext}.
   */
  public OpenIdContext getOpenIdContext()
  {
      return _context;
  }

  /**
   * Gets the identifier parameter - default is "openid_identifier".
   */
  public String getIdentifierParameter()
  {
      return _identifierParameter;
  }

  /**
   * Checks whether the relying party should automatically redirect the user if he
   * navigates back to the relying party's site.
   */
  public boolean isAutomaticRedirect()
  {
      return _automaticRedirect;
  }

  /**
   * Gets the auth redirection scheme.
   */
  public AuthRedirection getAuthRedirection()
  {
      return _authRedirection;
  }

  /**
   * Gets the user cache.
   */
  public UserCache getUserCache()
  {
      return _userCache;
  }

  /**
   * Return the current user,
   * either an already authenticated one,
   * or the one just discovered from the
   * <i>openid.identifier.parameter</i>
   * (= "openid_identifier" by default).<br>
   * Returns <code>null</code> if the {@link Constants#OPENID_MODE} associated
   * with the request is set to {@link Constants.Mode#CANCEL}
   * (in order to login under a different id),
   * or if the authentification is timed out.<br>
   * If returned user is <code>null</code>
   * and {@link #isAuthResponse(HttpServletRequest)} is <code>true</code>
   * then we have an authentication timeout.
   *
   * @param request HttpServletRequest
   * @return user OpenIdUser
   */
  public OpenIdUser discover(HttpServletRequest request)
  throws Exception
  {
      OpenIdUser user = (OpenIdUser)request.getAttribute(OpenIdUser.ATTR_NAME);
      if(user==null)
      {
          System.err.println("vvvvvv RelyingParty: user NOT in request");
          user = _manager.getUser(request);
          String id = null;
          if(user!=null)
          {
              System.err.println("vvvvvv RelyingParty: user in session");
              if(user.isAuthenticated())
              {
                  System.err.println("vvvvvv RelyingParty: user authenticated");
                  _listener.onAccess(user, request);
                  request.setAttribute(OpenIdUser.ATTR_NAME, user);
                  return user;
              }
              System.err.println("vvvvvv RelyingParty: user NOT authenticated");
              if((id=request.getParameter(_identifierParameter))==null)
              {
                  if(user.isAssociated())
                  {
                      String mode = request.getParameter(Constants.OPENID_MODE);
                      if(mode==null)
                          return _automaticRedirect ? user : null;

                      return Constants.Mode.CANCEL.equals(mode) ? null : user;
                  }

                  return user;
              }
              else if((id=id.trim()).length()!=0)
              {
                  Identifier identifier = Identifier.getIdentifier(id, _resolver, _context);
                  if(identifier.isResolved())
                  {
                      if(!identifier.getId().equals(user.getIdentifier()))
                      {
                          // new user or ... the user cancels authentication
                          // and provides a different openid identifier
                          return discover(identifier, request);
                      }
                  }
              }
          }
          else if((id=request.getParameter(_identifierParameter))!=null && (id=id.trim()).length()!=0)
          {
              System.err.println("vvvvvv RelyingParty: user NOT in session and parameter(" + _identifierParameter + ") is NOT empty");
              Identifier identifier = Identifier.getIdentifier(id, _resolver, _context);
              if(identifier.isResolved())
                  return discover(identifier, request);
          }
          else {
              System.err.println("vvvvvv RelyingParty: user NOT in session and parameter(" + _identifierParameter + ") is empty");
          }
      }
      return user;
  }

  protected OpenIdUser discover(Identifier identifier, HttpServletRequest request)
  throws Exception
  {
      OpenIdUser user = _userCache.get(identifier.getUrl(), true);
      if(user==null)
      {
          try
          {
              user = _context.getDiscovery().discover(identifier, _context);
          }
          catch(IOException e)
          {
              if(!_identifierAsServer)
                  throw e;
          }
          if(user==null)
          {
              if(!_identifierAsServer)
                  return null;

              // user = new OpenIdUser(identifier.getId(), YadisDiscovery.IDENTIFIER_SELECT, identifier.getUrl(), null);
              user = OpenIdUser.populate(identifier.getId(), YadisDiscovery.IDENTIFIER_SELECT, identifier.getUrl(), null);
          }

          _userCache.put(identifier.getUrl(), user);
      }

      _listener.onDiscovery(user, request);
      request.setAttribute(OpenIdUser.ATTR_NAME, user);
      return user;
  }

  /**
   * Returns true if the user has succeeded authentication on his openid provider;
   * The {@link OpenIdUser} is persisted if successful.
   */
  public boolean verifyAuth(OpenIdUser user, HttpServletRequest request,
          HttpServletResponse response) throws Exception
  {
      if(_context.getAssociation().verifyAuth(user, getAuthParameters(request), _context))
      {
          _listener.onAuthenticate(user, request);
          _manager.saveUser(user, request, response);
          return true;
      }
      return false;
  }

  /**
   * Returns true if the user is successfully associated with his openid provider;
   * The {@link OpenIdUser} is persisted if successful.
   */
  public boolean associate(OpenIdUser user, HttpServletRequest request,
          HttpServletResponse response) throws Exception
  {
      if(_context.getAssociation().associate(user, _context))
      {
          _manager.saveUser(user, request, response);
          return true;
      }
      return false;
  }

  /**
   * Returns true if the user is successfully associated and redirected to his
   * openid provider for authentication;
   * The {@link OpenIdUser} is persisted if successful.
   */
  public boolean associateAndAuthenticate(OpenIdUser user, HttpServletRequest request,
          HttpServletResponse response, String trustRoot, String realm,
          String returnTo) throws Exception
  {
      return _context.getAssociation().associate(user, _context) && authenticate(user, request,
              response, trustRoot, realm, returnTo);
  }

  public boolean authenticate(OpenIdUser user, HttpServletRequest request, HttpServletResponse response,
          String trustRoot, String realm, String returnTo) throws IOException
  {
      UrlEncodedParameterMap params = getAuthUrlMap(user, trustRoot, realm, returnTo);

      _listener.onPreAuthenticate(user, request, params);

      _manager.saveUser(user, request, response);

      _authRedirection.redirect(params, request, response);

      return true;
  }

  /**
   * Invalidates/terminates the openid session of the user associated with the given
   * {@code request};  To logout an authenticated user, you invoke this method.
   */
  public boolean invalidate(HttpServletRequest request, HttpServletResponse response)
  throws IOException
  {
      return _manager.invalidate(request, response);
  }

  /**
   * Adds a custom listener.
   */
  public RelyingParty addListener(Listener listener)
  {
      _listener.addListener(listener);
      return this;
  }

  /**
   * Adds a custom resolver.
   */
  public RelyingParty addResolver(Resolver resolver)
  {
      _resolver.addResolver(resolver);
      return this;
  }

  /**
   * Enables users to get notified on certain points of the openid authentication lifecycle.
   */
  public interface Listener
  {
      // the authentication process (in order)

      /**
       * Callback that gets called upon successful discovery.
       */
      public void onDiscovery(OpenIdUser user, HttpServletRequest request);

      /**
       * Callback that gets called before the user is redirected to this provider for
       * authentication.
       */
      public void onPreAuthenticate(OpenIdUser user, HttpServletRequest request,
              UrlEncodedParameterMap params);

      /**
       * Callback that gets called when the user has been redirected back to the
       * relying party's site and his authentication has been successfully verified.
       */
      public void onAuthenticate(OpenIdUser user, HttpServletRequest request);

      /**
       * Callback that gets called everytime an authenticated user accesses protected pages
       * from the relying party's site.
       * This could be used as a counter for page views.
       */
      public void onAccess(OpenIdUser user, HttpServletRequest request);
  }

  /**
   * A collection of listeners that wraps an array to delegate the methods from
   * the relying party {@link Listener listener}.
   */
  public static final class ListenerCollection implements Listener
  {

      private Listener[] _listeners = new Listener[]{};

      /**
       * Adds a listener.
       */
      public ListenerCollection addListener(Listener listener)
      {
          if(listener==null || indexOf(listener)!=-1)
              return this;

          synchronized(this)
          {
              Listener[] oldListeners = _listeners;
              Listener[] listeners = new Listener[oldListeners.length+1];
              System.arraycopy(oldListeners, 0, listeners, 0, oldListeners.length);
              listeners[oldListeners.length] = listener;
              _listeners = listeners;
          }

          return this;
      }

      /**
       * Gets the index of the listener on the wrapped array.
       */
      public int indexOf(Listener listener)
      {
          if(listener!=null)
          {
              Listener[] listeners = _listeners;
              for(int i=0; i<listeners.length; i++)
              {
                  if(listeners[i].equals(listener))
                      return i;
              }
          }
          return -1;
      }

      public void onDiscovery(OpenIdUser user, HttpServletRequest request)
      {
          Listener[] listeners = _listeners;
          for(int i=0,len=listeners.length; i<len; i++)
              listeners[i].onDiscovery(user, request);
      }

      public void onPreAuthenticate(OpenIdUser user, HttpServletRequest request,
              UrlEncodedParameterMap params)
      {
          Listener[] listeners = _listeners;
          for(int i=0,len=listeners.length; i<len; i++)
              listeners[i].onPreAuthenticate(user, request, params);
      }

      public void onAuthenticate(OpenIdUser user, HttpServletRequest request)
      {
          Listener[] listeners = _listeners;
          for(int i=0,len=listeners.length; i<len; i++)
              listeners[i].onAuthenticate(user, request);
      }

      public void onAccess(OpenIdUser user, HttpServletRequest request)
      {
          Listener[] listeners = _listeners;
          for(int i=0,len=listeners.length; i<len; i++)
              listeners[i].onAccess(user, request);
      }

  }

}
