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

package li.klass.fhem.adapter.devices.core.deviceItems;

import java.lang.reflect.Field;
import java.util.Locale;

import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.genericview.ShowFieldCache;

public class AnnotatedDeviceViewField extends AnnotatedDeviceViewItem {
    protected final Field field;
    private ShowField showField;

    public AnnotatedDeviceViewField(Field field) {
        this.field = field;
        field.setAccessible(true);
        this.showField = field.getAnnotation(ShowField.class);
        if (showField != null) {
            this.showField = new ShowFieldCache(showField);
        }
    }

    @Override
    public String getValueFor(Object object) {
        try {
            Object value = field.get(object);
            if (value == null) return null;

            return String.valueOf(value);
        } catch (IllegalAccessException e) {
            // this may never happen as we set the field as being accessible!
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getSortKey() {
        return field.getName().toLowerCase(Locale.getDefault());
    }

    @Override
    public String getKey() {
        return field.getName();
    }

    @Override
    public ShowField getShowFieldAnnotation() {
        return showField;
    }
}
