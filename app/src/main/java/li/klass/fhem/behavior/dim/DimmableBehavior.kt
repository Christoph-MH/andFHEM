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

package li.klass.fhem.behavior.dim

import android.content.Context
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry

class DimmableBehavior private constructor(
        val fhemDevice: FhemDevice,
        private val connectionId: String?,
        val behavior: DimmableTypeBehavior) {

    val currentDimPosition: Double
        get() = behavior.getCurrentDimPosition(fhemDevice)

    val dimLowerBound: Double
        get() = behavior.getDimLowerBound()

    val dimUpperBound: Double
        get() = behavior.getDimUpperBound()

    val dimStep: Double
        get() = behavior.getDimStep()


    fun getDimStateForPosition(position: Double): String =
            behavior.getDimStateForPosition(fhemDevice, position)

    suspend fun switchTo(stateUiService: StateUiService, context: Context, state: Double) {
        behavior.switchTo(stateUiService, context, fhemDevice, connectionId, state)
    }

    companion object {

        fun supports(device: FhemDevice) =
                !isDimDisabled(device)
                        && (ContinuousDimmableBehavior.supports(device)
                        || DiscreteDimmableBehavior.supports(device))

        fun behaviorFor(fhemDevice: FhemDevice, connectionId: String?): DimmableBehavior? {
            val setList = fhemDevice.setList
            if (isDimDisabled(fhemDevice)) {
                return null
            }

            val discrete = DiscreteDimmableBehavior.behaviorFor(setList)
            if (discrete != null) {
                return DimmableBehavior(fhemDevice, connectionId, discrete)
            }

            val continuous = ContinuousDimmableBehavior.behaviorFor(setList)
            if (continuous != null) {
                return DimmableBehavior(fhemDevice, connectionId, continuous)
            }

            return null
        }

        fun continuousBehaviorFor(device: FhemDevice, attribute: String, connectionId: String?): DimmableBehavior? {
            val setList = device.setList
            if (!setList.contains(attribute)) {
                return null
            }
            val setListSliderValue = setList[attribute, true] as SliderSetListEntry
            return DimmableBehavior(device, connectionId, ContinuousDimmableBehavior(setListSliderValue, attribute))
        }


        fun isDimDisabled(device: FhemDevice): Boolean {
            val disableDim = device.xmlListDevice.attributes["disableDim"]
            return disableDim != null && "true".equals(disableDim.value, ignoreCase = true)
        }
    }
}
