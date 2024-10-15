package org.starcode.unittests.mockito.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.starcode.exceptions.RequiredObjectIsNullException
import org.starcode.repository.BookRepository
import org.starcode.services.BookService
import org.starcode.unittests.mocks.MockBook
import java.time.LocalDate
import java.time.ZoneId

import java.util.*

@ExtendWith(MockitoExtension::class)
class BookServiceTest {

    private lateinit var inputObject: MockBook

    @InjectMocks
    private lateinit var service: BookService

    @Mock
    private lateinit var repository: BookRepository

    @BeforeEach
    fun setUpMock() {
        inputObject = MockBook()
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun findById() {
        val book = inputObject.mockEntity(1)
        book.id = 1
        `when`(repository.findById(1)).thenReturn(Optional.of(book))

        val result = service.findById(1)

        assertNotNull(result)
        assertNotNull(result.key)
        assertNotNull(result.links)
        assertTrue(result.links.toString().contains("</api/v1/book/1>;rel=\"self\""))
        assertEquals("Title Test1",result.title)
        assertEquals("Author Test1",result.author)
        assertEquals(1.0,result.price)

    }

    @Test
    fun create() {
        val entity = inputObject.mockEntity(1)

        val persisted = entity.copy()
        persisted.id = 1

        `when`(repository.save(entity)).thenReturn(persisted)

        val vo = inputObject.mockVO(1)
        val result = service.create(vo)

        assertNotNull(result)
        assertNotNull(result.key)
        assertNotNull(result.links)
        assertTrue(result.links.toString().contains("</api/v1/book/1>;rel=\"self\""))
        assertEquals("Title Test1",result.title)
        assertEquals("Author Test1",result.author)
        assertEquals(1.0,result.price)
        val localDate = LocalDate.of(2000, 1, 1) //Gambiarra pois usando Date(yyyy,mm,dd) o Ano nao estava sendo convertido corretamente
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        assertEquals(date, result.launchDate)
    }

    @Test
    fun createWithNullBook(){
        val exception: Exception = assertThrows(
            RequiredObjectIsNullException::class.java
        ) { service.create(null)}

        val expectedMessage = "Its is not allowed to persist a null object"
        val actualMessage = exception.message

        assertTrue(actualMessage!!.contains(expectedMessage))

    }


    @Test
    fun update() {
        val entity = inputObject.mockEntity(1)

        val persisted = entity.copy()
        persisted.id = 1

        `when`(repository.findById(1)).thenReturn(Optional.of(entity))
        `when`(repository.save(entity)).thenReturn(persisted)

        val vo = inputObject.mockVO(1)
        val result = service.update(vo)

        assertNotNull(result)
        assertNotNull(result.key)
        assertNotNull(result.links)
        assertTrue(result.links.toString().contains("</api/v1/book/1>;rel=\"self\""))
        assertEquals("Title Test1",result.title)
        assertEquals("Author Test1",result.author)
        assertEquals(1.0,result.price)
        val localDate = LocalDate.of(2000, 1, 1) //Gambiarra pois usando Date(yyyy,mm,dd) o Ano nao estava sendo convertido corretamente
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        assertEquals(date, result.launchDate)
    }

    @Test
    fun updateWithNullBook(){
        val exception: Exception = assertThrows(
            RequiredObjectIsNullException::class.java
        ) { service.update(null)}

        val expectedMessage = "Its is not allowed to persist a null object"
        val actualMessage = exception.message

        assertTrue(actualMessage!!.contains(expectedMessage))

    }

    @Test
    fun delete() {
        val entity = inputObject.mockEntity(1)

        `when`(repository.findById(1)).thenReturn(Optional.of(entity))

        service.delete(1)
    }
}