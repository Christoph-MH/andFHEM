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

package li.klass.fhem.room.list.backend

import li.klass.fhem.devices.list.backend.HiddenRoomsDeviceFilter
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.fhemweb.FhemWebConfigurationService
import javax.inject.Inject

class ViewableRoomListService @Inject constructor(private val deviceListService: DeviceListService,
                                                  private val hiddenRoomsDeviceFilter: HiddenRoomsDeviceFilter,
                                                  private val fhemWebConfigurationService: FhemWebConfigurationService) {
    fun sortedRoomNameList(connectionId: String? = null): List<String> {
        val allDevicesList = deviceListService.getAllRoomsDeviceList(connectionId)
        val withoutHiddenGroups = fhemWebConfigurationService.filterHiddenGroupsFrom(allDevicesList)
        val withoutHiddenDevices = hiddenRoomsDeviceFilter.filterHiddenDevicesIfRequired(withoutHiddenGroups)

        val containedRooms = withoutHiddenDevices.allDevices
                .flatMap { it.getRooms() }
                .distinct()

        val withoutHiddenRooms = fhemWebConfigurationService.filterHiddenRoomsIn(containedRooms)
        return fhemWebConfigurationService.sortRooms(withoutHiddenRooms)
    }
}