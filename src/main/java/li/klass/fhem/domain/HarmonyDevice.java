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

package li.klass.fhem.domain;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.room.list.backend.xmllist.DeviceNode;

import static li.klass.fhem.domain.core.DeviceFunctionality.REMOTE_CONTROL;

public class HarmonyDevice extends FhemDevice {
    @ShowField(description = ResourceIdMapper.activity, showInOverview = true, showInDetail = false)
    private String activity;

    @XmllistAttribute("activity")
    public void setActivity(String activity, DeviceNode deviceNode) {
        this.activity = activity;
        setMeasured(deviceNode.getMeasured());
    }

    public String getActivity() {
        return activity;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return REMOTE_CONTROL;
    }
}
