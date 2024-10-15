package org.starcode.unittests.mocks

import org.starcode.model.Book
import org.starcode.data.vo.BookVO
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.ArrayList
import java.util.Date

class MockBook {

    fun mockEntity(): Book {
        return mockEntity(0)
    }

    fun mockVO(): BookVO {
        return mockVO(0)
    }

    fun mockEntityList(): ArrayList<Book> {
        val books: ArrayList<Book> = ArrayList<Book>()
        for (i in 0..13) {
            books.add(mockEntity(i))
        }
        return books
    }

    fun mockVOList(): ArrayList<BookVO> {
        val books: ArrayList<BookVO> = ArrayList()
        for (i in 0..13) {
            books.add(mockVO(i))
        }
        return books
    }

    fun mockEntity(number: Int): Book {
        val book = Book()
        book.title = "Title Test$number"
        book.author = "Author Test$number"
        book.price = number.toDouble()
        book.id = number.toLong()
        val localDate = LocalDate.of(2000, 1, number)
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        book.launchDate = date
        return book
    }

    fun mockVO(number: Int): BookVO {
        val book = BookVO()
        book.title = "Title Test$number"
        book.author = "Author Test$number"
        book.price = number.toDouble()
        book.key = number.toLong()
        val localDate = LocalDate.of(2000, 1, number)
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        book.launchDate = date
        return book
    }

}