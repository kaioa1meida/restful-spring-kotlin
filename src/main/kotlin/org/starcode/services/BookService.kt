package org.starcode.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.stereotype.Service
import org.starcode.controller.BookController
import org.starcode.data.vo.BookVO
import org.starcode.exceptions.RequiredObjectIsNullException
import org.starcode.exceptions.ResourceNotFoundException
import org.starcode.mapper.DozerMapper
import org.starcode.model.Book
import org.starcode.repository.BookRepository
import java.util.logging.Logger

@Service
class BookService {

    @Autowired
    private lateinit var repository: BookRepository
    @Autowired
    private lateinit var assembler: PagedResourcesAssembler<BookVO>

    private val logger = Logger.getLogger(BookService::class.java.name)

    fun findAll(pageable: Pageable): PagedModel<EntityModel<BookVO>> {
        logger.info("Finding all books!")

        val books = repository.findAll(pageable)

        val vos = books.map { b -> DozerMapper.parseObject(b, BookVO::class.java) }

        vos.map { b -> b.add(linkTo(BookController::class.java).slash(b.key).withSelfRel()) }

        return assembler.toModel(vos)
    }

    fun findBookByTitle(title: String, pageable: Pageable): PagedModel<EntityModel<BookVO>> {
        logger.info("Finding Books by Title!")

        val books = repository.findBookByTitle(title, pageable)

        val vos = books.map { b -> DozerMapper.parseObject(b, BookVO::class.java) }

        vos.map { b -> b.add(linkTo(BookController::class.java).slash(b.key).withSelfRel()) }

        return assembler.toModel(vos)
    }

    fun findById(id: Long): BookVO {
        logger.info("Finding a one book with ID: $id!")

        val book = repository.findById(id)
            .orElseThrow{ ResourceNotFoundException("No records found for this ID!") }

        val bookVO =  DozerMapper.parseObject(book, BookVO::class.java)
        val withSelfRel = linkTo(BookController::class.java).slash(bookVO.key).withSelfRel()
        bookVO.add(withSelfRel)

        return bookVO
    }

    fun create(book: BookVO?): BookVO {
        if (book == null) throw RequiredObjectIsNullException()

        logger.info("Creating a one book with title: ${book.title}!")

        val entity: Book = DozerMapper.parseObject(book, Book::class.java)
        val bookVO: BookVO = DozerMapper.parseObject(repository.save(entity), BookVO::class.java)

        val withSelfRel = linkTo(BookController::class.java).slash(bookVO.key).withSelfRel()
        bookVO.add(withSelfRel)

        return bookVO
    }

    fun update(book: BookVO?): BookVO {
        if (book == null) throw RequiredObjectIsNullException()

        logger.info("Updating a one book with ID: ${book.key}!")

        val entity = repository.findById(book.key)
            .orElseThrow{ ResourceNotFoundException("No records found for this ID!") }

        entity.title = book.title
        entity.author = book.author
        entity.launchDate = book.launchDate
        entity.price = book.price

        val bookVO: BookVO = DozerMapper.parseObject(repository.save(entity), BookVO::class.java)

        val withSelfRel = linkTo(BookController::class.java).slash(bookVO.key).withSelfRel()
        bookVO.add(withSelfRel)

        return bookVO
    }

    fun delete(id: Long) {
        logger.info("Deleting a one book with ID: $id!")

        val entity = repository.findById(id)
            .orElseThrow {ResourceNotFoundException("No records found for this ID!") }

        return repository.delete(entity)
    }
}