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

package li.klass.fhem.devices.list.backend

import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.update.backend.fhemweb.FhemWebConfigurationService
import li.klass.fhem.util.ApplicationProperties
import javax.inject.Inject

class HiddenRoomsDeviceFilter @Inject constructor(
        val applicationProperties: ApplicationProperties,
        val fhemWebConfigurationService: FhemWebConfigurationService
) {
    fun filterHiddenDevicesIfRequired(roomDeviceList: RoomDeviceList): RoomDeviceList {
        val showHiddenDevices = applicationProperties.getBooleanSharedPreference(SettingsKeys.SHOW_HIDDEN_DEVICES, false)
        if (showHiddenDevices) {
            return roomDeviceList
        }

        val hiddenRooms = fhemWebConfigurationService.getHiddenRooms()
        return roomDeviceList.filter { !it.isInAnyRoomsOf(hiddenRooms) }
    }
}