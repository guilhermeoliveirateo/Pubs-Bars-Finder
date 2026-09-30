package com.pubsbarsfinder.app.models

interface PubStore {

    /** findAll: returns a list with all stored pubs*/
    fun findAll(): List<PubModel>

    /** create: the store assigns a new id and returns the saved pub */
    fun create(pub: PubModel): PubModel

    /** update: returns false if no pub with that id exists */
    fun update(pub: PubModel): Boolean

    /** delete: returns false if no pub with that id exists */
    fun delete(id: Long): Boolean

    /** findOne: returns null if no pub with that id exists */
    fun findOne(id: Long): PubModel?
}