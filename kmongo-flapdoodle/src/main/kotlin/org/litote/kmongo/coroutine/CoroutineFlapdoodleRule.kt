/*
 * Copyright (C) 2016 Litote
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litote.kmongo.coroutine

import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.MongoClient
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import kotlinx.coroutines.experimental.runBlocking
import org.bson.types.ObjectId
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.litote.kmongo.async.AsyncFlapdoodleRule
import org.litote.kmongo.async.AsyncTestClient
import org.litote.kmongo.util.KMongoUtil
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.reflect.KClass

/**
 * A [org.junit.Rule] to help writing tests for KMongo using [Flapdoodle](http://flapdoodle-oss.github.io/de.flapdoodle.embed.mongo/).
 */
class CoroutineFlapdoodleRule<T : Any>(val defaultDocumentClass: KClass<T>,
                                       val generateRandomCollectionName: Boolean = false) : TestRule {


    companion object {

        val mongoClient: MongoClient = AsyncTestClient.instance
        var databaseName: String = "test"
        val database: MongoDatabase by lazy {
            mongoClient.getDatabase(databaseName)
        }

        private suspend inline fun <T> singleResult(crossinline callback: (SingleResultCallback<T>) -> Unit): T? {
            return suspendCoroutine { continuation ->
                callback(SingleResultCallback { result: T?, throwable: Throwable? ->
                    if (throwable != null) {
                        continuation.resumeWithException(throwable)
                    } else {
                        continuation.resume(result)
                    }
                })
            }
        }

        inline fun <reified T : Any> getCollection(): MongoCollection<T>
                = database.getCollection(KMongoUtil.defaultCollectionName(T::class), T::class.java)

        fun <T : Any> getCollection(clazz: KClass<T>): MongoCollection<T>
                = database.getCollection(KMongoUtil.defaultCollectionName(clazz), clazz.java)

        suspend inline fun <reified T : Any> dropCollection()
                = dropCollection(KMongoUtil.defaultCollectionName(T::class))

        suspend fun dropCollection(clazz: KClass<*>)
                = dropCollection(KMongoUtil.defaultCollectionName(clazz))

        suspend fun dropCollection(collectionName: String)
                = database.getCollection(collectionName).drop()

        suspend fun <T> MongoCollection<T>.drop(): Void? {
            return singleResult { this.drop(it) }
        }

    }

    val col: MongoCollection<T> by lazy {
        val name = if (generateRandomCollectionName) {
            ObjectId().toString()
        } else {
            KMongoUtil.defaultCollectionName(defaultDocumentClass)
        }

        AsyncFlapdoodleRule.getCollection(name, defaultDocumentClass)
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            override fun evaluate() {
                try {
                    base.evaluate()
                } finally {
                    runBlocking { col.drop() }
                }
            }
        }
    }

}