package org.starcode.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.starcode.model.Book

@Repository
interface BookRepository: JpaRepository<Book, Long>