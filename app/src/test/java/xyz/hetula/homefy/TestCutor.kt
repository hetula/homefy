/*
 * Copyright (c) 2018 Tuomo Heino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.hetula.homefy

import java.util.*
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

class TestCutor : AbstractExecutorService() {
    override fun isTerminated() = true

    override fun execute(command: Runnable?) {
        command?.run()
    }

    override fun shutdown() {
        // Instant!
    }

    override fun shutdownNow(): MutableList<Runnable> = Collections.emptyList()

    override fun isShutdown() = true

    override fun awaitTermination(timeout: Long, unit: TimeUnit?) = true
}