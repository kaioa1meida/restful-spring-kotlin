package org.starcode.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.starcode.model.Book
import org.starcode.model.User

@Repository
interface UserRepository: JpaRepository<User?, Long?> {

    @Query("SELECT u FROM User u WHERE u.userName =:userName")
    fun findByUsername(@Param("userName") userName: String?): User?
}