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

import li.klass.fhem.R;
import li.klass.fhem.domain.genericview.DeviceChart;
import li.klass.fhem.domain.genericview.ShowInDetail;
import li.klass.fhem.domain.genericview.ShowInOverview;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueUtil;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class KS300Device extends Device<KS300Device> implements Serializable {

    @ShowInOverview(description = R.string.temperature)
    @ShowInDetail(description = R.string.temperature)
    private String temperature;

    @ShowInOverview(description = R.string.wind)
    @ShowInDetail(description = R.string.wind)
    private String wind;

    @ShowInOverview(description = R.string.humidity)
    @ShowInDetail(description = R.string.humidity)
    private String humidity;

    @ShowInOverview(description = R.string.rain)
    @ShowInDetail(description = R.string.rain)
    private String rain;

    @ShowInDetail(description = R.string.avgDay)
    private String averageDay;

    @ShowInDetail(description = R.string.avgMonth)
    private String averageMonth;

    @ShowInDetail(description = R.string.isRaining)
    private String isRaining;

    @Override
    public int compareTo(KS300Device ks300Device) {
        return getName().compareTo(ks300Device.getName());
    }

    @Override
    public void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = ValueUtil.formatTemperature(nodeContent);
        } else if (keyValue.equals("WIND")) {
            this.wind = ValueDescriptionUtil.appendKmH(nodeContent);
        } else if (keyValue.equals("HUMIDITY")) {
            this.humidity = ValueDescriptionUtil.appendPercent(nodeContent);
        } else if (keyValue.equals("RAIN")) {
            this.rain = ValueDescriptionUtil.appendLm2(nodeContent);
        } else if (keyValue.equals("AVG_DAY")) {
            this.averageDay = nodeContent;
        } else if (keyValue.equals("AVG_MONTH")) {
            this.averageMonth = nodeContent;
        } else if (keyValue.equals("ISRAINING")) {
            this.isRaining = nodeContent;
        }
    }

    @Override
    public String toString() {
        return "KS300Device{" +
                "temperature='" + temperature + '\'' +
                ", wind='" + wind + '\'' +
                ", humidity='" + humidity + '\'' +
                ", rain='" + rain + '\'' +
                "} " + super.toString();
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        addDeviceChartIfNotNull(temperature, new DeviceChart(R.string.temperatureGraph, R.string.yAxisTemperature,
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:IR:")));
        addDeviceChartIfNotNull(humidity, new DeviceChart(R.string.humidityGraph, R.string.yAxisHumidity,
                new ChartSeriesDescription(R.string.temperature, "6:IR:")));
        addDeviceChartIfNotNull(wind, new DeviceChart(R.string.windGraph, R.string.yAxisWind,
                new ChartSeriesDescription(R.string.wind, "8:IR:")));
        addDeviceChartIfNotNull(rain, new DeviceChart(R.string.rainGraph, R.string.yAxisRain,
                new ChartSeriesDescription(R.string.rain, "10:IR:")));
    }
}
