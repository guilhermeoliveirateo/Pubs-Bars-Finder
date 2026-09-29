package com.pubsbarsfinder.app.models

import java.util.concurrent.atomic.AtomicLong

class PubMemStore : PubStore {

    private val idGenerator = AtomicLong(0)
    private val pubs: ArrayList<PubModel> = ArrayList()

    override fun findAll(): List<PubModel> {
        return pubs.toList()
    }

    override fun create(pub: PubModel): PubModel {
        val newId = idGenerator.incrementAndGet()
        val newPub = pub.copy(id = newId)
        pubs.add(newPub)
        return newPub
    }

    override fun update(pub: PubModel): Boolean {
        val index = pubs.indexOfFirst { it.id == pub.id }
        if (index == -1) {
            return false
        } else {
            pubs[index] = pub
            return true
        }
    }

    override fun delete(id: Long): Boolean {
        return pubs.removeIf { it.id == id }
    }

    override fun findOne(id: Long): PubModel? {
        for (pub in pubs) {
            if (pub.id == id) {
                return pub
            }
        }
        return null
    }
}