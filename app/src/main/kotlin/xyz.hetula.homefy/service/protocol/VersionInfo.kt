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

package xyz.hetula.homefy.service.protocol

import com.google.gson.annotations.Expose

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
data class VersionInfo(@Expose val server_id: String,
                       @Expose val name: String,
                       @Expose val version: String,
                       @Expose val databaseId: String,
                       @Expose val authentication: AuthType) {
    @Expose var databaseSize = 0

    override fun toString(): String {
        return "VersionInfo{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", authentication=" + authentication +
                '}'
    }

    enum class AuthType {
        NONE,
        BASIC,
        OAUTH2
    }
}
