package com.pubsbarsfinder.app.main

import com.pubsbarsfinder.app.models.PubMemStore
import com.pubsbarsfinder.app.models.PubModel
import com.pubsbarsfinder.app.models.PubStore

private val store: PubStore = PubMemStore()

fun main() {
    println("=== Pub Finder Console ===")

    var option: Int

    do {
        option = menu()
        when (option) {
            1 -> addPub()
            2 -> listPubs()
            3 -> searchPub()
            4 -> updatePub()
            5 -> deletePub()
            6 -> exit()
            else -> println("Invalid option, please try again.")
        }
    } while (option != 6)
}

fun menu(): Int {
    println()
    println("1. Add a pub")
    println("2. List all pubs")
    println("3. Search pub by id")
    println("4. Update a pub")
    println("5. Delete a pub")
    println("6. Exit")
    print("Choose and option: ")
    return readlnOrNull()?.toIntOrNull() ?: -1
}

fun readTitle(): String {
    while(true) {
        print("Title: ")
        val title = readlnOrNull()?.trim().orEmpty()
        if (title.isNotEmpty()) return title
        println("Title can't be empty, please try again.")
    }
}

fun readCoordinate(label: String, min: Double, max: Double): Double {
    while(true) {
        print("$label: ")
        val coordinate: Double? = readlnOrNull()?.trim()?.toDoubleOrNull()
        if (coordinate != null && coordinate in min..max) {
            return coordinate
        } else {
            println("$label must be a number between $min and $max")
        }
    }
}

fun addPub() {

    val title = readTitle()
    print("Description: ")
    val description = readlnOrNull()?.trim().orEmpty()
    val latitude = readCoordinate("Latitude", -90.0, 90.0)
    val longitude = readCoordinate("Longitude", -180.0, 180.0)

    val pub = PubModel(title = title, description = description, latitude = latitude, longitude = longitude)

    val newPub = store.create(pub)

    print("Pub added with ID ${newPub.id}.")
}

fun listPubs() {
    val pubs = store.findAll()
    if (pubs.isEmpty()) {
        println("No pubs found.")
    } else {
        pubs.forEach {
            println("ID: ${it.id} | Title: ${it.title} | Description: ${it.description} | Latitude: ${it.latitude} | Longitude: ${it.longitude}")
        }
    }
}

fun searchPub() {

}

fun updatePub() {

}

fun deletePub() {

}

fun exit() {
    println("Goodbye!!")
}