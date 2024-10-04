package org.starcode.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.starcode.model.Book
import org.starcode.model.Person

@Repository
interface BookRepository: JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE b.title LIKE LOWER(CONCAT ('%',:title,'%'))")
    fun findBookByTitle(@Param("title") title: String, pageable: Pageable) : Page<Book>
}