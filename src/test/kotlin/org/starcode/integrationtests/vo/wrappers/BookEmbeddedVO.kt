package org.starcode.integrationtests.vo.wrappers

import com.fasterxml.jackson.annotation.JsonProperty
import org.starcode.integrationtests.vo.BookVO

class BookEmbeddedVO {

    @JsonProperty("bookVOList")
    var books: List<BookVO>? = null
}