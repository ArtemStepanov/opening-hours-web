package ru.stxima.openinghoursweb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpeningHoursWebApplication

fun main(args: Array<String>) {
    runApplication<OpeningHoursWebApplication>(*args)
}
