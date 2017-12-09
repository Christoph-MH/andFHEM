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

import org.junit.Test;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.assertj.core.api.Assertions.assertThat;

public class NetatmoDeviceTest extends DeviceXMLParsingBase {

    @Test
    public void should_read_Netatmo_DEVICE() {
        GenericDevice device = getDeviceFor("netatmo_device", GenericDevice.class);

        assertThat(device.getAlias()).isEqualTo("Indoor");
        assertThat(stateValueFor(device, "co2")).isEqualTo("650 (ppm)");
        assertThat(stateValueFor(device, "humidity")).isEqualTo("66.0 (%)");
        assertThat(stateValueFor(device, "noise")).isEqualTo("52 (dB)");
        assertThat(stateValueFor(device, "pressure")).isEqualTo("960.2 (hPa)");
        assertThat(stateValueFor(device, "temperature")).isEqualTo("26.6 (°C)");

        assertThat(device.isSupported()).isTrue();
    }

    @Test
    public void should_read_Netatmo_MODULE() {
        GenericDevice device = getDeviceFor("netatmo_module", GenericDevice.class);

        assertThat(device.getAlias()).isEqualTo("Outdoor");
        assertThat(stateValueFor(device, "humidity")).isEqualTo("60.0 (%)");
        assertThat(stateValueFor(device, "temperature")).isEqualTo("28.2 (°C)");

        assertThat(device.isSupported()).isTrue();
    }

    @Override
    protected String getFileName() {
        return "netatmo.xml";
    }
}
