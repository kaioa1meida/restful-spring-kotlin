package org.starcode.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.starcode.model.Person

@Repository
interface PersonRepository: JpaRepository<Person, Long?> {}