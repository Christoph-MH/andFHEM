/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.infrastructure

import android.support.test.espresso.matcher.BoundedMatcher
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.adapter.rooms.DeviceGroupAdapter
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher


object CustomMatchers {
    object DeviceList {
        fun withDeviceName(deviceName: String): Matcher<out RecyclerView.ViewHolder>? {
            return object : BaseMatcher<RecyclerView.ViewHolder>() {
                override fun describeTo(description: Description?) {
                }

                override fun matches(item: Any?): Boolean {
                    if (item !is DeviceGroupAdapter.ViewHolder.ForDevice) {
                        return false
                    }
                    return item.itemView.findViewById<TextView>(R.id.deviceName)
                            .text == deviceName
                }
            }
        }
    }

    object Android {

        fun withToolbarTitle(
                textMatcher: Matcher<String>): Matcher<Any> {
            return object : BoundedMatcher<Any, Toolbar>(Toolbar::class.java) {
                public override fun matchesSafely(toolbar: Toolbar): Boolean =
                        textMatcher.matches(toolbar.title)

                override fun describeTo(description: Description) {
                    description.appendText("with toolbar title: ")
                    textMatcher.describeTo(description)
                }
            }
        }
    }
}