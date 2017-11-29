/*
 * MIT License
 *
 * Copyright (c) 2017 Tuomo Heino
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.hetula.homefy

import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
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
    val loadingActivity = ActivityTestRule<MainActivity>(MainActivity::class.java, false, false)

    @Before
    fun initialize() {
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
            waitSome(500)
            loadingActivity.runOnUiThread {
                pagesConsumer(arrayOf())
            }
        }

        onView(withId(R.id.btn_connect)).perform(click())
        onView(withText(R.string.enter_server_address)).check(matches(isDisplayed()))

        onView(withId(R.id.txt_address)).perform(replaceText(url))
        assertEquals("Url already set!", "", protocol.server)
        onView(withId(R.id.btn_connect)).perform(click())
        assertEquals("Url Mismatch!", url, protocol.server)
        onView(withText(R.string.loading)).check(matches(isDisplayed()))

        waitSome(750)
        onView(withText(R.string.nav_music)).check(matches(isDisplayed()))
        onView(withText(R.string.nav_albums)).check(matches(isDisplayed()))
        onView(withText(R.string.nav_artists)).check(matches(isDisplayed()))
        onView(withText(R.string.nav_favs)).check(matches(isDisplayed()))
        onView(withText(R.string.nav_playlists)).check(matches(isDisplayed()))
        onView(withText(R.string.nav_search)).check(matches(isDisplayed()))
    }
}
