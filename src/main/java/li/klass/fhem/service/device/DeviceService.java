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

package li.klass.fhem.service.device;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.room.RoomListService;

/**
 * Class accumulating all device actions like renaming, moving or deleting.
 */
@Singleton
public class DeviceService {

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    RoomListService roomListService;

    @Inject
    public DeviceService() {
    }

    /**
     * Rename a device.
     *
     * @param device  concerned device
     * @param newName new device name
     * @param context context
     */
    public void renameDevice(final FhemDevice device, final String newName, Context context) {
        commandExecutionService.executeSafely("rename " + device.getName() + " " + newName, Optional.<String>absent(), context);
        device.getXmlListDevice().setInternal("NAME", newName);
    }

    /**
     * Deletes a device.
     *
     * @param device concerned device
     */
    public void deleteDevice(final FhemDevice device, Context context) {
        commandExecutionService.executeSafely("delete " + device.getName(), Optional.<String>absent(), context);
        RoomDeviceList roomDeviceList = roomListService.getRoomDeviceList(Optional.<String>absent(), context);
        if (roomDeviceList != null) {
            roomDeviceList.removeDevice(device, context);
        }
    }

    /**
     * Sets an alias for a device.
     *
     * @param device  concerned device
     * @param alias   new alias to set
     * @param context context
     */
    public void setAlias(final FhemDevice device, final String alias, Context context) {
        if (Strings.isNullOrEmpty(alias)) {
            commandExecutionService.executeSafely("deleteattr " + device.getName() + " alias", Optional.<String>absent(), context);
        } else {
            commandExecutionService.executeSafely("attr " + device.getName() + " alias " + alias, Optional.<String>absent(), context);
        }
        device.getXmlListDevice().setAttribute("alias", alias);
    }

    /**
     * Moves a device.
     *
     * @param device              concerned device
     * @param newRoomConcatenated new room to move the concerned device to.
     * @param context             context
     */
    public void moveDevice(final FhemDevice device, final String newRoomConcatenated, Context context) {
        commandExecutionService.executeSafely("attr " + device.getName() + " room " + newRoomConcatenated, Optional.<String>absent(), context);

        device.setRoomConcatenated(newRoomConcatenated);

        context.sendBroadcast(new Intent(Actions.DO_UPDATE));
    }
}
