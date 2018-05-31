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

import android.content.Context
import android.content.Intent
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.hetula.homefy.service.HomefyService
import xyz.hetula.homefy.service.TestBase
import xyz.hetula.homefy.service.protocol.VersionInfo

@RunWith(AndroidJUnit4::class)
class LoadingTests : TestBase() {

    @Rule
    @JvmField
    val loadingActivity = ActivityTestRule<HomefyActivity>(HomefyActivity::class.java, false, false)

    @Before
    fun initialize() {
        InstrumentationRegistry.getTargetContext().getSharedPreferences("HomefyActivity", Context.MODE_PRIVATE).edit().clear().commit()
        createServices()
        loadingActivity.launchActivity(Intent())
    }

    @After
    fun clean() {
        loadingActivity.finishActivity()
        context.stopService(Intent(context, HomefyService::class.java))
    }

    @Test
    fun setupLoadingNoSongs() {
        val url = "http://test.org"
        protocol.requestVersionInfo = { reqUrl, callback ->
            assertEquals("$url/version", reqUrl)
            loadingActivity.runOnUiThread {
                callback(VersionInfo("TestingZZ", "Testfy", "1.0", "none", VersionInfo.AuthType.NONE))
            }
        }
        protocol.requestPages = { pageLength, pagesConsumer ->
            assertEquals(context.resources.getInteger(R.integer.load_page_song_count), pageLength)
            waitSome(1500)
            loadingActivity.runOnUiThread {
                pagesConsumer(arrayOf())
            }
        }

        onView(withId(R.id.btn_connect)).perform(click())
        onView(withText(R.string.setup_enter_server_address)).check(matches(isDisplayed()))

        onView(withId(R.id.txt_address)).perform(replaceText(url))
        assertEquals("Url already set!", "", protocol.server)
        onView(withId(R.id.btn_connect)).perform(click())
        assertEquals("Url Mismatch!", url, protocol.server)
    }
}
