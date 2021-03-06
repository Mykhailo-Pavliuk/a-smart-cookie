package com.a_smart_cookie.controller.command.guest;

import com.a_smart_cookie.controller.command.Command;
import com.a_smart_cookie.controller.route.HttpHandlerType;
import com.a_smart_cookie.controller.route.HttpPath;
import com.a_smart_cookie.controller.route.WebPath;
import com.a_smart_cookie.dao.EntityColumn;
import com.a_smart_cookie.entity.Role;
import com.a_smart_cookie.entity.Status;
import com.a_smart_cookie.entity.User;
import com.a_smart_cookie.exception.HashingException;
import com.a_smart_cookie.exception.ServiceException;
import com.a_smart_cookie.service.ServiceFactory;
import com.a_smart_cookie.service.UserService;
import com.a_smart_cookie.util.RecaptchaHandler;
import com.a_smart_cookie.util.hashing.PBKDF2Hash;
import com.a_smart_cookie.util.validation.user.UserValidator;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

/**
 * Provides with sign in mechanism for user.
 */
public class LoginCommand extends Command {

	private static final long serialVersionUID = -6045416586328449454L;

	private static final Logger LOG = Logger.getLogger(LoginCommand.class);

	@Override
	public HttpPath execute(HttpServletRequest request, HttpServletResponse response) {
		LOG.debug("Command starts");

		String email = request.getParameter("email");
		String password = request.getParameter("password");

		HttpSession session = request.getSession();

		HttpPath notValidHttpPath = performValidationMechanism(email, password, session);
		if (notValidHttpPath != null) return notValidHttpPath;

		LOG.trace("User is valid");

		String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
		LOG.trace("gRecaptchaResponse=" + gRecaptchaResponse);

		// Verify CAPTCHA.
		if (!RecaptchaHandler.verify(gRecaptchaResponse)) {
			session.setAttribute("invalidCaptcha", true);
			setOldEmailToSession(email, session);
			LOG.debug("Command finished with invalid captcha");
			return new HttpPath(WebPath.Command.SIGN_IN, HttpHandlerType.SEND_REDIRECT);
		}

		Optional<User> user;
		try {
			UserService userService = ServiceFactory.getInstance().getUserService();
			user = userService.getUserByEmail(email);
		} catch (ServiceException e) {
			session.setAttribute("serviceError", true);
			LOG.error("Exception has occurred on service layer", e);
			return new HttpPath(WebPath.Command.SIGN_IN, HttpHandlerType.SEND_REDIRECT);
		}

		if (user.isEmpty()) {
			session.setAttribute("badCredentials", true);
			setOldEmailToSession(email, session);
			LOG.debug("Command finished with not found user");
			return new HttpPath(WebPath.Command.SIGN_IN, HttpHandlerType.SEND_REDIRECT);
		}

		try {
			if (!PBKDF2Hash.verifyHash(password, user.get().getSalt(), user.get().getPassword())) {
				session.setAttribute("badCredentials", true);
				setOldEmailToSession(email, session);
				LOG.debug("Command finished with not equals passwords");
				return new HttpPath(WebPath.Command.SIGN_IN, HttpHandlerType.SEND_REDIRECT);
			}
		} catch (HashingException e) {
			session.setAttribute("serviceError", true);
			LOG.error("Can't perform hashing", e);
			return new HttpPath(WebPath.Command.SIGN_IN, HttpHandlerType.SEND_REDIRECT);
		}

		if (user.get().getStatus() == Status.BLOCKED) {
			session.setAttribute("isBlocked", true);
			setOldEmailToSession(email, session);
			LOG.debug("Command finished with blocked user");
			return new HttpPath(WebPath.Command.SIGN_IN, HttpHandlerType.SEND_REDIRECT);
		}

		session.invalidate();
		session = request.getSession();
		session.setAttribute("user", user.get());
		LOG.trace("user --> " + user);

		LOG.debug("Command finished with signed in user");

		if (user.get().getRole() == Role.ADMIN) {
			return new HttpPath(WebPath.Command.ADMIN_USERS, HttpHandlerType.SEND_REDIRECT);
		}

		return new HttpPath(WebPath.Command.CATALOG_FIRST_PAGE, HttpHandlerType.SEND_REDIRECT);
	}

	private HttpPath performValidationMechanism(String email, String password, HttpSession session) {
		Map<String, Boolean> validationResult = UserValidator.getValidationResults(email, password);

		if (validationResult.containsValue(false)) {
			session.setAttribute("isValidEmail", validationResult.get(EntityColumn.User.EMAIL.getName()));
			session.setAttribute("isValidPassword", validationResult.get(EntityColumn.User.PASSWORD.getName()));

			LOG.debug("Command finished with not valid user");
			return new HttpPath(WebPath.Command.SIGN_IN, HttpHandlerType.SEND_REDIRECT);
		}
		return null;
	}

	private void setOldEmailToSession(String email, HttpSession session) {
		session.setAttribute("oldLoginEmail", email);
	}

}
