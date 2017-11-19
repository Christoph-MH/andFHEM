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

import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

@OverviewViewSettings(showState = true)
@SuppressWarnings("unused")
public class EIBDevice extends DimmableContinuousStatesDevice<EIBDevice> {

    private String model;

    @XmllistAttribute("model")
    public void setModel(String value) {
        if (value.equals("dpt10")) {
            value = "time";
        }
        model = value;
    }

    @Override
    public void afterDeviceXMLRead(Context context) {
        super.afterDeviceXMLRead(context);

        if ("percent".equalsIgnoreCase(model) && "???".equalsIgnoreCase(getInternalState())) {
            setState("0 (%)");
        }

        if (model == null || model.equals("time") || model.equals("date") ||
                getInternalState().equalsIgnoreCase("???")) return;

        double value = ValueExtractUtil.extractLeadingDouble(getInternalState());
        String description = "";
        if (model.equalsIgnoreCase("speedsensor")) {
            description = ValueDescriptionUtil.M_S;
        } else if (model.equalsIgnoreCase("tempsensor")) {
            description = ValueDescriptionUtil.C;
        } else if (model.equalsIgnoreCase("brightness") || model.equalsIgnoreCase("lightsensor")) {
            description = ValueDescriptionUtil.LUX;
        } else if (model.equalsIgnoreCase("percent")) {
            description = ValueDescriptionUtil.PERCENT;
            int percent = ValueExtractUtil.extractLeadingInt(getInternalState());
            setState(ValueDescriptionUtil.append(percent, description));
            return;
        }

        setState(ValueDescriptionUtil.append(value, description));
    }

    public String getModel() {
        return model;
    }

    @Override
    public boolean supportsToggle() {
        return !(model != null && model.equalsIgnoreCase("time")) &&
                getXmlListDevice().getSetList().contains("on", "off");
    }

    @Override
    public String getDimStateNameForDimStateValue(float value) {
        return "value " + (int) value + "";
    }

    @Override
    public float getPositionForDimState(String dimState) {
        dimState = dimState.replace("value", "").trim();
        return ValueExtractUtil.extractLeadingFloat(dimState);
    }

    @Override
    public boolean supportsDim() {
        return model != null && model.equals("percent");
    }

    @Override
    public String formatStateTextToSet(String stateToSet) {
        if (stateToSet.startsWith("value")) {
            stateToSet = stateToSet.replace("value", "").trim();
        }

        if (model != null && model.equals("percent")) {
            return ValueDescriptionUtil.appendPercent(stateToSet);
        }
        return super.formatStateTextToSet(stateToSet);
    }

    @Override
    protected String getSetListDimStateAttributeName() {
        return "value";
    }
}
