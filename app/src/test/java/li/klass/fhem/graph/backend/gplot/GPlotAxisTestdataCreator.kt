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
package li.klass.fhem.graph.backend.gplot

import com.google.common.collect.Range
import li.klass.fhem.graph.backend.gplot.GPlotSeriesTestdataBuilder.defaultGPlotSeries

object GPlotAxisTestdataCreator {
    @JvmStatic
    fun defaultGPlotAxis(): GPlotAxis {
        val defaultGPlotSeries = defaultGPlotSeries()
        return GPlotAxis("myLabel", Range.atLeast(10.0), series = listOf(
                defaultGPlotSeries.copy(viewSpec = defaultGPlotSeries.viewSpec.copy(title = "series1", axis = GPlotSeries.Axis.LEFT,
                        color = GPlotSeries.SeriesColor.RED)),
                defaultGPlotSeries.copy(viewSpec = defaultGPlotSeries.viewSpec.copy(title = "series2", axis = GPlotSeries.Axis.RIGHT,
                        color = GPlotSeries.SeriesColor.GREEN))

        ))
    }
}