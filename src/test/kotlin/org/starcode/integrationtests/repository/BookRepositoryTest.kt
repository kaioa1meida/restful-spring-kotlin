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
import org.starcode.model.Book
import org.starcode.repository.BookRepository


@ExtendWith(SpringExtension::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(Lifecycle.PER_CLASS)
class BookRepositoryTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var repository: BookRepository

    private lateinit var book: Book

    @BeforeAll
    fun setupTests() {
        book = Book()
    }

    @Test
    @Order(0)
    fun testFindBookByTitle() {
        val pageable: Pageable = PageRequest.of(0,12, Sort.by(Sort.Direction.ASC, "title"))

        book = repository.findBookByTitle("java", pageable).content[0]

        assertEquals("JavaScript", book.title)
        assertEquals("Crockford", book.author)
        assertEquals(67.00, book.price)
    }
}