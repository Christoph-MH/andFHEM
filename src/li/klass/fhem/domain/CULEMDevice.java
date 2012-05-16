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
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import org.w3c.dom.NamedNodeMap;

import java.util.List;

public class CULEMDevice extends Device<CULEMDevice> {

    private String currentUsage;
    private String dayUsage;
    private String monthUsage;
    private double sumGraphDivisionFactor = 1d;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("CURRENT")) {
            currentUsage = nodeContent + " (kwh)";
        } else if (keyValue.equals("CUM_DAY")) {
            dayUsage = extractCumUsage(nodeContent, "CUM_DAY") + " (kwh)";
        } else if (keyValue.equals("CUM_MONTH")) {
            monthUsage = extractCumUsage(nodeContent, "CUM_MONTH") + " (kwh)";
        } else if (keyValue.equals("SUM_GRAPH_DIVISION_FACTOR")) {
            sumGraphDivisionFactor = Double.valueOf(nodeContent);
        }
    }

    public String getCurrentUsage() {
        return currentUsage;
    }

    public String getDayUsage() {
        return dayUsage;
    }

    public String getMonthUsage() {
        return monthUsage;
    }

    public double getSumGraphDivisionFactor() {
        return sumGraphDivisionFactor;
    }

    private String extractCumUsage(String cumString, String cumToken) {
        cumToken = cumToken + ": ";
        return cumString.substring(cumToken.length(), cumString.indexOf(" ", cumToken.length() + 1));
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        addDeviceChartIfNotNull(currentUsage, new DeviceChart(R.string.usageGraph, R.string.yAxisUsage,
                ChartSeriesDescription.getSumInstance(R.string.currentUsage, "8::0:", getSumGraphDivisionFactor())));
    }
}
