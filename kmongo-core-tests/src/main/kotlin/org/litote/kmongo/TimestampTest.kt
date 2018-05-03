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

package org.litote.kmongo

import org.bson.BsonTimestamp
import org.junit.Test
import org.litote.kmongo.TimestampTest.TimestampValue
import kotlin.test.assertEquals

/**
 *
 */
class TimestampTest : AllCategoriesKMongoBaseTest<TimestampValue>() {

    data class TimestampValue(val date: BsonTimestamp?)

    @Test
    fun testInsertAndLoad() {
        val value = TimestampValue(BsonTimestamp(1, 1))
        col.insertOne(value)
        val loaded = col.findOne()
        assertEquals(value, loaded)
    }

    @Test
    fun testNullInsertAndLoad() {
        val value = TimestampValue(null)
        col.insertOne(value)
        val loaded = col.findOne()
        assertEquals(value, loaded)
    }
}