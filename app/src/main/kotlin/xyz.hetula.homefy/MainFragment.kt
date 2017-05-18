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
 *
 */

package xyz.hetula.homefy

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import xyz.hetula.homefy.library.LibraryFragment
import xyz.hetula.homefy.player.PlayerFragment

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater!!.inflate(R.layout.fragment_main, container, false) as FrameLayout
        val viewPager = main.findViewById(R.id.viewPager) as ViewPager
        viewPager.adapter = HomefyPagerAdapter(fragmentManager)
        // Clear any backstack entries, just in case
        val stackCount = fragmentManager.backStackEntryCount
        for (i in 0..stackCount - 1) {
            fragmentManager.popBackStackImmediate()
        }
        return main
    }

    private class HomefyPagerAdapter
    internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            if (position == 1) return PlayerFragment()
            return LibraryFragment()
        }

        override fun getCount(): Int {
            return 2
        }
    }
}
