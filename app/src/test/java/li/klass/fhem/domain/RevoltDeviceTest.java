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

public class RevoltDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void should_read_device_attributes() {
        GenericDevice device = getDeviceFor("Revolt_Kueche", GenericDevice.class);
        assertThat(device).isNotNull();
        assertThat(stateValueFor(device, "power")).isEqualTo("113.3 (W)");
        assertThat(stateValueFor(device, "energy")).isEqualTo("1.25 (kWh)");
        assertThat(stateValueFor(device, "pf")).isEqualTo("0.98");
        assertThat(stateValueFor(device, "frequency")).isEqualTo("50.0 (Hz)");
    }

    @Override
    protected String getFileName() {
        return "revolt.xml";
    }
}
