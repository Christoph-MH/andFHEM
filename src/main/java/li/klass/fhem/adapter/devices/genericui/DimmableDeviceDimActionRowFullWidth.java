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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.content.Intent;
import android.widget.TableRow;

import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.room.list.backend.xmllist.XmlListDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class DimmableDeviceDimActionRowFullWidth extends DeviceDimActionRowFullWidth {

    public DimmableDeviceDimActionRowFullWidth(FhemDevice device, int layoutId, TableRow updateRow) {
        this((DimmableDevice) device, layoutId, updateRow);
    }

    private DimmableDeviceDimActionRowFullWidth(DimmableDevice device, int layoutId, TableRow updateRow) {
        super(device.getDimPosition(), device.getDimLowerBound(), device.getDimStep(),
                device.getDimUpperBound(), updateRow, layoutId);
    }

    @Override
    public void onStopDim(Context context, XmlListDevice device, float progress) {
        context.startService(new Intent(Actions.DEVICE_DIM)
                .setClass(context, DeviceIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_DIM_PROGRESS, progress)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context)));
    }

    @Override
    public String toDimUpdateText(XmlListDevice device, float progress) {
        return progress + "";
    }
}
