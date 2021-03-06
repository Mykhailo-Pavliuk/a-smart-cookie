<%@ page pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jspf/directive/page.jspf" %>
<%@ include file="/WEB-INF/jspf/directive/taglib.jspf" %>
<%@ include file="/WEB-INF/jspf/directive/locale.jspf" %>
<%@ page import="com.a_smart_cookie.util.validation.user.UserValidationPattern" %>

<html>

<fmt:message key="sign_in_jsp.title" var="signInTitle"/>
<c:set var="title" value="${signInTitle}" scope="page"/>
<%@ include file="/WEB-INF/jspf/head.jspf" %>

<script src='https://www.google.com/recaptcha/api.js?hl=${cookie['lang'].value}'></script>

<body>

<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="content">
	<div class="container">
		<h1 class="text-center"><fmt:message key="sign_in_jsp.title"/></h1>
		<div class="row justify-content-center">
			<form action="${pageContext.request.contextPath}/controller?command=login" method="post"
				  class="col-6">
				<div class="form-group">
					<label for="email"><fmt:message key="credentials.email"/></label>
					<input
							type="email"
							class="form-control"
							name="email"
							id="email"
							required
							pattern="${UserValidationPattern.EMAIL.pattern}"
							title="<fmt:message key="validation.user.email" />"
							<c:choose>

								<c:when test="${sessionScope.oldLoginEmail != null}">
									value="${sessionScope.oldLoginEmail}"
								</c:when>

								<c:when test="${sessionScope.registeredEmail != null}">
										value="${sessionScope.registeredEmail}"
								</c:when>

							</c:choose>
					>
					<c:if test="${sessionScope.isValidEmail != null && !sessionScope.isValidEmail}">
					<span class="error text-danger">
						<fmt:message key="validation.user.email"/>
					</span>
					</c:if>
				</div>
				<div class="form-group">
					<label for="password"><fmt:message key="credentials.password"/></label>
					<input
							type="password"
							class="form-control"
							name="password"
							id="password"
							required
							pattern="${UserValidationPattern.PASSWORD.pattern}"
							title="<fmt:message key="validation.user.password" />"
					>
					<c:if test="${sessionScope.isValidPassword != null && !sessionScope.isValidPassword}">
					<span class="error text-danger">
						<fmt:message key="validation.user.password"/>
					</span>
					</c:if>
				</div>
				<c:if test="${sessionScope.badCredentials != null && sessionScope.badCredentials}">
					<span class="error text-danger">
						<fmt:message key="sign_in_jsp.bad_credentials"/>
					</span>
				</c:if>
				<c:if test="${sessionScope.isBlocked != null && sessionScope.isBlocked}">
					<span class="error text-danger">
						<fmt:message key="sign_in_jsp.blocked_user"/>
					</span>
				</c:if>

				<!-- reCAPTCHA -->
				<div class="g-recaptcha d-flex justify-content-center"
					 data-sitekey="6LfEvLQeAAAAAG6XVyOjCNvDIOoi6Xoa1Xd_8kyU">
				</div>
				<c:if test="${sessionScope.invalidCaptcha != null && sessionScope.invalidCaptcha}">
					<span class="error text-danger d-flex justify-content-center">
						<fmt:message key="sign_in_jsp.invalid_captcha"/>
					</span>
				</c:if>

				<div class="col text-center">
					<button type="submit" class="btn btn-primary mt-2">
						<fmt:message key="sign_in_jsp.button_sign_in"/>
					</button>
				</div>
			</form>
		</div>
		<div class="row justify-content-center text-center mt-3">
			<div class="col-6">
				<p>
					<fmt:message key="sign_in_jsp.dont_have_acc_question"/>
					<a href="${pageContext.request.contextPath}/controller?command=sign-up">
						<fmt:message key="sign_in_jsp.register"/>
					</a>
				</p>
			</div>
		</div>
	</div>
</div>

<c:if test="${not empty sessionScope.serviceError}">
	${sar:remove(pageContext.session, "serviceError")}
	<div class="alert alert-danger alert-dismissable text-center">
		<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
		<fmt:message key="alert.service_exception"/>
	</div>
</c:if>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

</body>