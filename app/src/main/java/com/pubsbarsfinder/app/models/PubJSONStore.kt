package com.pubsbarsfinder.app.models

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class PubJSONStore(private val file: File) : PubStore {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val listType = object : TypeToken<ArrayList<PubModel>>() {}.type

    private val pubs = ArrayList<PubModel>()
    private val idGenerator = AtomicLong(0)

    init {
        deserialize()
    }

    override fun findAll(): List<PubModel> {
        return pubs.toList()
    }

    override fun findOne(id: Long): PubModel? {
        for (pub in pubs) {
            if (pub.id == id) {
                return pub
            }
        }
        return null
    }

    override fun create(pub: PubModel): PubModel {
        val newId = idGenerator.incrementAndGet()
        val newPub = pub.copy(id = newId)
        pubs.add(newPub)
        serialize()
        return newPub
    }

    override fun update(pub: PubModel): Boolean {
        val index = pubs.indexOfFirst { it.id == pub.id }
        if (index == -1) {
            return false
        } else {
            pubs[index] = pub
            serialize()
            return true
        }
    }

    override fun delete(id: Long): Boolean {
        val removed = pubs.removeIf { it.id == id }
        if (removed) serialize()
        return removed
    }

    override fun deleteAll() {
        pubs.clear()
        serialize()
    }

    private fun serialize() {
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(pubs, listType))
        Timber.i("Saved ${pubs.size} pubs to ${file.name}")
    }

    private fun deserialize() {
        if (!file.exists()) return
        try {
            val loaded: ArrayList<PubModel> = gson.fromJson(file.readText(), listType) ?: arrayListOf()
            pubs.clear()
            pubs.addAll(loaded)
            idGenerator.set(pubs.maxOfOrNull { it.id } ?: 0L)
            Timber.i("Loaded ${pubs.size} pubs from ${file.name}")
        } catch (e: JsonSyntaxException) {
            Timber.e(e, "Could not read ${file.name}, starting with an empty list")
        }
    }
}