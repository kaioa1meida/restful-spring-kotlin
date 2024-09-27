package org.starcode.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.starcode.controller.BookController
import org.starcode.data.vo.AccountCredentialsVO
import org.starcode.data.vo.BookVO
import org.starcode.data.vo.TokenVO
import org.starcode.exceptions.RequiredObjectIsNullException
import org.starcode.exceptions.ResourceNotFoundException
import org.starcode.mapper.DozerMapper
import org.starcode.model.Book
import org.starcode.repository.BookRepository
import org.starcode.repository.UserRepository
import org.starcode.security.jwt.JwtTokenProvider
import java.util.logging.Logger

@Service
class AuthService {

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var tokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var repository: UserRepository

    private val logger = Logger.getLogger(AuthService::class.java.name)

    fun signin(data: AccountCredentialsVO): ResponseEntity<*>{
        logger.info("Trying log user ${data.username}")
        return try {
            val username = data.username
            val password = data.password
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
            val user = repository.findByUsername(username)
            val tokenResponse: TokenVO = if (user != null) {
                tokenProvider.createAccessToken(username!!, user.roles)
            } else {
                throw UsernameNotFoundException("Username $username not found!")
            }
            ResponseEntity.ok(tokenResponse)
        } catch (e: AuthenticationException) {
            throw BadCredentialsException("Invalid Username or Password supplied!")
        }
    }
    fun refreshToken(username: String, refreshToken: String): ResponseEntity<*>{
        logger.info("Trying get refresh token to user $username")

        val user = repository.findByUsername(username)
        val tokenResponse: TokenVO = if (user != null) {
            tokenProvider.refreshToken(refreshToken)
        } else {
            throw UsernameNotFoundException("Username $username not found!")
        }
        return ResponseEntity.ok(tokenResponse)
    }
}