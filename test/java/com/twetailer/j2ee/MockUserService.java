package com.twetailer.j2ee;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;

public class MockUserService implements UserService {

	public String createLoginURL(String arg0) {
		return null;
	}

	public String createLogoutURL(String arg0) {
		return null;
	}

	public User getCurrentUser() {
		return null;
	}

	public boolean isUserAdmin() {
		return false;
	}

	public boolean isUserLoggedIn() {
		return false;
	}

}
