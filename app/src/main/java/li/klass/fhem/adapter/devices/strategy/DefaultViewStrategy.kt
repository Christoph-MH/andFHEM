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

package li.klass.fhem.adapter.devices.strategy

import android.view.LayoutInflater
import android.view.View
import android.widget.TableRow
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.DevStateIconAdder
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceViewItem
import li.klass.fhem.domain.core.FhemDevice
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
open class DefaultViewStrategy @Inject
constructor(
        private val devStateIconAdder: DevStateIconAdder
) : ViewStrategy() {

    override fun createOverviewView(layoutInflater: LayoutInflater, convertView: View?, rawDevice: FhemDevice, deviceItems: List<XmlDeviceViewItem>, connectionId: String?): View {
        var myView = convertView
        val stopWatch = StopWatch()
        stopWatch.start()
        if (myView == null || myView.tag == null) {
            myView = layoutInflater.inflate(overviewLayout, null)
            val viewHolder = GenericDeviceOverviewViewHolder(myView)
            myView!!.tag = viewHolder
            LOGGER.debug("createOverviewView - inflating layout, device=" + rawDevice.name + ", time=" + stopWatch.time)
        } else {
            LOGGER.debug("createOverviewView - reusing generic device overview view for device=" + rawDevice.name)
        }
        val viewHolder = myView.tag as GenericDeviceOverviewViewHolder
        fillDeviceOverviewView(myView, rawDevice, viewHolder, deviceItems, layoutInflater)
        LOGGER.debug("createOverviewView - finished, device=" + rawDevice.name + ", time=" + stopWatch.time)
        return myView
    }

    override fun supports(fhemDevice: FhemDevice): Boolean = true

    private val overviewLayout: Int
        get() = R.layout.device_overview_generic

    open fun fillDeviceOverviewView(view: View, device: FhemDevice, viewHolder: GenericDeviceOverviewViewHolder, items: List<XmlDeviceViewItem>, layoutInflater: LayoutInflater) {
        viewHolder.resetHolder()
        setTextView(viewHolder.deviceName, device.aliasOrName)

        try {
            var currentGenericRow = 0

            val overviewItems = items.filter { it.isShowInOverview }
            val state = items.firstOrNull { it.key == "STATE" }
            val addState = overviewItems.none { it.desc != view.context.getString(R.string.measured) }
            val stateToAdd = if (addState) state else null

            val toShow = listOfNotNull(stateToAdd) + overviewItems

            toShow.forEach { item ->
                currentGenericRow++
                val rowHolder: GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder
                if (currentGenericRow <= viewHolder.tableRowCount) {
                    rowHolder = viewHolder.getTableRowAt(currentGenericRow - 1)
                } else {
                    rowHolder = createTableRow(layoutInflater, R.layout.device_overview_generic_table_row)
                    viewHolder.addTableRow(rowHolder)
                }
                fillTableRow(rowHolder, item, device)
                viewHolder.tableLayout.addView(rowHolder.row)
            }
        } catch (e: Exception) {
            LOGGER.error("exception occurred while setting device overview values", e)
        }
    }

    private fun createTableRow(inflater: LayoutInflater, resource: Int): GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder {
        val tableRow = inflater.inflate(resource, null) as TableRow
        return GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder(
                tableRow,
                tableRow.findViewById(R.id.description),
                tableRow.findViewById(R.id.value),
                tableRow.findViewById(R.id.devStateIcon)
        )
    }

    private fun fillTableRow(holder: GenericDeviceOverviewViewHolder.GenericDeviceTableRowHolder, item: XmlDeviceViewItem, device: FhemDevice) {
        setTextView(holder.description, item.desc)
        setTextView(holder.value, item.value)
        if (item.value == "") {
            holder.row.visibility = View.GONE
        } else {
            holder.row.visibility = View.VISIBLE
        }

        devStateIconAdder.addDevStateIconIfRequired(item.value, device, holder.devStateIcon)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultViewStrategy::class.java)
    }
}
