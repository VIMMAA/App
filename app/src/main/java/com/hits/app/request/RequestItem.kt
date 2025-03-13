package com.hits.app.request

import java.time.LocalDate

class RequestItem(
    val number: Int,
    val applicationDate: LocalDate,
    val status: Status
) {
    constructor(number: Int) : this(number, LocalDate.now(), Status.WAITING)
}