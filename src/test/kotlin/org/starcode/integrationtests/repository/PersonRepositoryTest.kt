package org.starcode.integrationtests.repository

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.starcode.integrationtests.testcontainers.AbstractIntegrationTest
import org.starcode.model.Person
import org.starcode.repository.PersonRepository

@ExtendWith(SpringExtension::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(Lifecycle.PER_CLASS)
class PersonRepositoryTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var repository: PersonRepository

    private lateinit var person: Person

    @BeforeAll
    fun setupTests() {
        person = Person()
    }

    @Test
    @Order(0)
    fun testFindByName() {
        val pageable: Pageable = PageRequest.of(0,12,Sort.by(Sort.Direction.ASC, "firstName"))

        person = repository.findPersonByName("ayr", pageable).content[0]

        assertEquals("Ayrton", person.firstName)
        assertEquals("Senna", person.lastName)
        assertEquals("São Paulo", person.address)
        assertEquals("Male", person.gender)
        assertEquals(true, person.enabled)
    }

    @Test
    @Order(1)
    fun testDisablePerson() {

        repository.disablePerson(person.id)

        person = repository.findById(person.id).get()

        assertEquals("Ayrton", person.firstName)
        assertEquals("Senna", person.lastName)
        assertEquals("São Paulo", person.address)
        assertEquals("Male", person.gender)
        assertEquals(false, person.enabled)
    }
}