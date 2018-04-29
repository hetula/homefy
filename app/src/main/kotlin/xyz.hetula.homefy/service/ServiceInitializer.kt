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

package xyz.hetula.homefy.service

import xyz.hetula.homefy.library.HomefyLibrary
import xyz.hetula.homefy.player.HomefyPlayer
import xyz.hetula.homefy.playlist.HomefyPlaylist
import xyz.hetula.homefy.service.protocol.DefaultHomefyProtocol
import xyz.hetula.homefy.service.protocol.HomefyProtocol

internal object ServiceInitializer {
    internal var protocol: (Unit) -> HomefyProtocol = { DefaultHomefyProtocol() }

    internal var library: (HomefyProtocol) -> HomefyLibrary = { protocol -> HomefyLibrary(protocol) }

    internal var player: (HomefyProtocol, HomefyLibrary) -> HomefyPlayer = { protocol, library -> HomefyPlayer(protocol, library) }

    internal var playlist: (Unit) -> HomefyPlaylist = { HomefyPlaylist() }
}
