package com.hits.app.application

import java.time.LocalDate

class ApplicationItem(
    val id: String,
    val number: Int,
    val applicationDate: String,
    val status: String
) {
    constructor(number: Int) : this("0", number, LocalDate.now().toString(), "NotDefined")
}