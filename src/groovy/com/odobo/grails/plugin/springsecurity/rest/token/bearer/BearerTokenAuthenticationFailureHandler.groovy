package com.odobo.grails.plugin.springsecurity.rest.token.bearer

import com.odobo.grails.plugin.springsecurity.rest.token.reader.TokenReader
import groovy.util.logging.Slf4j
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Handles authentication failure when BearerToken authentication is enabled.
 */
@Slf4j
class BearerTokenAuthenticationFailureHandler implements AuthenticationFailureHandler {

    TokenReader tokenReader

    /**
     * Sends the proper response code and headers, as defined by RFC6750.
     *
     * @param request
     * @param response
     * @param e
     * @throws IOException
     * @throws ServletException
     */
    @Override
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {

        String headerValue
        int status
        def token = tokenReader.findToken(request, response)
        def matchesBearerSpecPreconditions = tokenReader.matchesBearerSpecPreconditions(request, response)

        if (token && !matchesBearerSpecPreconditions) {
            headerValue = 'Bearer error="invalid_token"'
            status = HttpServletResponse.SC_UNAUTHORIZED
        } else if (!token && matchesBearerSpecPreconditions) {
            headerValue = 'Bearer '
            status = HttpServletResponse.SC_UNAUTHORIZED
        } else if(token && matchesBearerSpecPreconditions) {
            headerValue = 'Bearer error="invalid_token"'
            status = HttpServletResponse.SC_UNAUTHORIZED
        }else if (!token && !matchesBearerSpecPreconditions) {
            headerValue = 'Bearer error="invalid_request"'
            status = HttpServletResponse.SC_BAD_REQUEST
        }

        response.addHeader('WWW-Authenticate', headerValue)
        response.status = status

        log.debug "Sending status code ${response.status} and header WWW-Authenticate: ${response.getHeader('WWW-Authenticate')}"
    }
}
