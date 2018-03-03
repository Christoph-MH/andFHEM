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

package li.klass.fhem.adapter.devices.genericui.onoff

import android.content.Context
import li.klass.fhem.R
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice

class OnOffSubStateActionRow(layoutId: Int, private val subState: String, connectionId: String?, val stateUiService: StateUiService)
    : AbstractOnOffActionRow(layoutId, R.string.blank, connectionId) {

    override fun isOn(device: FhemDevice, context: Context): Boolean {
        val offStateName = getOffStateName(device, context)
        return !offStateName.equals(device.xmlListDevice.getState(subState, true).or("off"), ignoreCase = true)
    }

    override fun onButtonClick(context: Context, device: FhemDevice, connectionId: String?, targetState: String) {
        stateUiService.setSubState(device.xmlListDevice, subState, targetState, connectionId, context)
    }
}
