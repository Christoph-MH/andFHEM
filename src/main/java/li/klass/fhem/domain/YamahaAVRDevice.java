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

import android.content.Context;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.multimedia.VolumeDevice;
import li.klass.fhem.resources.ResourceIdMapper;

import static li.klass.fhem.util.ValueExtractUtil.onOffToTrueFalse;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true, showMeasured = true)
public class YamahaAVRDevice extends ToggleableDevice implements VolumeDevice {

    @XmllistAttribute(value = {"volume", "volume_level"})
    private int volume;

    private boolean muted;

    @XmllistAttribute("input")
    private String input;

    @Override
    public boolean supportsToggle() {
        return true;
    }

    @ShowField(description = ResourceIdMapper.musicVolume)
    public String getVolumeDesc() {
        return volume + "";
    }

    @XmllistAttribute("mute")
    public void setMute(String value) {
        muted = onOffToTrueFalse(value);
    }

    @Override
    public float getVolumeAsFloat() {
        return volume;
    }

    public boolean isMuted() {
        return muted;
    }


    public String getInput() {
        return input;
    }

    @Override
    public void afterDeviceXMLRead(Context context) {
        super.afterDeviceXMLRead(context);
        deviceFunctionality = DeviceFunctionality.REMOTE_CONTROL.getCaptionText(context);
    }

}
