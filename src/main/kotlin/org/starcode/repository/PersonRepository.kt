package org.starcode.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.starcode.model.Person

@Repository
interface PersonRepository: JpaRepository<Person, Long?> {

    @Modifying
    @Query("UPDATE Person p SET p.enabled = false WHERE p.id =:id")
    fun disablePerson(@Param("id") id: Long?)
}