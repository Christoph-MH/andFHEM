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

package li.klass.fhem.update.backend.xmllist;

import org.joda.time.DateTime;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static li.klass.fhem.util.DateFormatUtil.FHEM_DATE_FORMAT;

public class DeviceNode implements Serializable {
    public enum DeviceNodeType {
        INT, ATTR, STATE, HEADER, GCM_UPDATE
    }

    private String key;
    private String value;
    private DateTime measured;
    private DeviceNodeType nodeType;

    public DeviceNode(DeviceNodeType nodeType, String key, String value, String measured) {
        this(nodeType, key, value, parseMeasured(measured));
    }

    private static DateTime parseMeasured(String measured) {
        try {
            return measured == null ? null : FHEM_DATE_FORMAT.parseDateTime(measured);
        } catch (Exception e) {
            return null;
        }
    }

    public DeviceNode(DeviceNodeType nodeType, String key, String value, DateTime measured) {
        this.key = checkNotNull(key);
        this.value = checkNotNull(value);
        this.nodeType = checkNotNull(nodeType);
        this.measured = measured;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public DateTime getMeasured() {
        return measured;
    }

    public DeviceNodeType getType() {
        return nodeType;
    }

    public boolean isStateNode() {
        return nodeType == DeviceNodeType.STATE;
    }

    @Override
    public String toString() {
        return "DeviceNode{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", measured='" + measured + '\'' +
                ", nodeType=" + nodeType +
                '}';
    }
}
