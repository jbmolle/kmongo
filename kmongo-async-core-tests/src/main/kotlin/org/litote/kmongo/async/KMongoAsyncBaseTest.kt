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
package org.litote.kmongo.async

import com.mongodb.async.client.MongoCollection
import org.junit.Rule
import org.junit.experimental.categories.Category
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoRootTest
import org.litote.kmongo.NativeMappingCategory
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

/**
 *
 */
@Category(JacksonMappingCategory::class, NativeMappingCategory::class)
open class KMongoAsyncBaseTest<T : Any> : KMongoRootTest() {

    @Suppress("LeakingThis")
    @Rule
    @JvmField
    val rule = AsyncFlapdoodleRule(getDefaultCollectionClass())

    val col by lazy { rule.col }

    val database by lazy { rule.database }

    inline fun <reified T : Any> getCollection(): MongoCollection<T> = rule.getCollection<T>()

    inline fun <reified T : Any> dropCollection() = rule.dropCollection<T>()

    fun asyncTest(testToRun: () -> Unit) = rule.asyncTest(testToRun)

    @Suppress("UNCHECKED_CAST")
    fun getDefaultCollectionClass(): KClass<T> =
        ((this::class.java.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>).kotlin

}