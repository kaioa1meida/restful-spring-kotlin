package org.starcode.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.starcode.controller.BookController
import org.starcode.data.vo.BookVO
import org.starcode.exceptions.RequiredObjectIsNullException
import org.starcode.exceptions.ResourceNotFoundException
import org.starcode.mapper.DozerMapper
import org.starcode.model.Book
import org.starcode.repository.BookRepository
import org.starcode.repository.UserRepository
import java.util.logging.Logger

@Service
class UserService(@field:Autowired var repository: UserRepository) : UserDetailsService  { //Outro metodo de injeção de dependencia

    private val logger = Logger.getLogger(UserService::class.java.name)

    override fun loadUserByUsername(username: String?): UserDetails {
        logger.info("Finding a one user by username: $username!")

        val user = repository.findByUsername(username)

        return user ?: throw UsernameNotFoundException("Username $username not found")
    }

}