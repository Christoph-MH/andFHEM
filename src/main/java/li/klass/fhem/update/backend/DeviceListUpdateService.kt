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

package li.klass.fhem.update.backend

import android.app.Application
import android.content.Context
import android.content.Intent
import com.google.common.base.Optional
import li.klass.fhem.appindex.AppIndexIntentService
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import li.klass.fhem.update.backend.xmllist.DeviceListParser
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceListUpdateService @Inject constructor(
        private val commandExecutionService: CommandExecutionService,
        private val deviceListParser: DeviceListParser,
        private val deviceListHolderService: DeviceListHolderService,
        private val application: Application
) {

    fun updateSingleDevice(deviceName: String, connectionId: String? = null): UpdateResult =
            executeXmllistPartial(connectionId, deviceName)

    fun updateRoom(roomName: String, connectionId: String? = null): UpdateResult =
            executeXmllistPartial(connectionId, "room=" + roomName)

    fun updateAllDevices(connectionId: String? = null): UpdateResult {
        return executeXmllist(connectionId, "", object : UpdateHandler {
            override fun handle(cached: RoomDeviceList, parsed: RoomDeviceList): RoomDeviceList = parsed
        })
    }

    fun getLastUpdate(connectionId: String?): DateTime =
            deviceListHolderService.getLastUpdate(Optional.fromNullable(connectionId), applicationContext)

    private fun update(context: Context, connectionId: Optional<String>, result: Optional<RoomDeviceList>): Boolean {
        var success = false
        if (result.isPresent) {
            success = deviceListHolderService.storeDeviceListMap(result.get(), connectionId, context)
            if (success) LOG.info("update - update was successful, sending result")
        } else {
            LOG.info("update - update was not successful, sending empty device list")
        }
        return success
    }

    private fun executeXmllistPartial(connectionId: String?, devSpec: String): UpdateResult {
        LOG.info("executeXmllist(devSpec={}) - fetching xmllist from remote", devSpec)
        return executeXmllist(connectionId, " " + devSpec, object : UpdateHandler {
            override fun handle(cached: RoomDeviceList, parsed: RoomDeviceList): RoomDeviceList {
                cached.addAllDevicesOf(parsed, applicationContext)
                return cached
            }
        })
    }

    private fun executeXmllist(connectionId: String?, xmllistSuffix: String, updateHandler: UpdateHandler):
            UpdateResult {
        val context = applicationContext
        val connection = Optional.fromNullable(connectionId)
        val command = Command("xmllist" + xmllistSuffix, connection)
        val result = commandExecutionService.executeSync(command, context) ?: ""
        val roomDeviceList = parseResult(connection, context, result, updateHandler)
        val success = update(context, connection, roomDeviceList)

        return when (success) {
            true -> {
                context.startService(Intent("com.google.firebase.appindexing.UPDATE_INDEX")
                        .setClass(context, AppIndexIntentService::class.java))
                UpdateResult.Success(roomDeviceList.orNull())
            }
            else -> UpdateResult.Error()
        }
    }

    private fun parseResult(connectionId: Optional<String>, context: Context, result: String, updateHandler: UpdateHandler): Optional<RoomDeviceList> {
        val parsed = Optional.fromNullable(deviceListParser.parseAndWrapExceptions(result, context))
        val cached = deviceListHolderService.getCachedRoomDeviceListMap(connectionId, context)
        if (parsed.isPresent) {
            val newDeviceList = updateHandler.handle(cached.or(parsed.get()), parsed.get())
            deviceListHolderService.storeDeviceListMap(newDeviceList, connectionId, context)
            return Optional.of(newDeviceList)
        }
        return Optional.absent<RoomDeviceList>()
    }

    private val applicationContext: Context get() = application.applicationContext

    private interface UpdateHandler {
        fun handle(cached: RoomDeviceList, parsed: RoomDeviceList): RoomDeviceList
    }

    sealed class UpdateResult {
        class Success(val roomDeviceList: RoomDeviceList?) : UpdateResult()
        class Error : UpdateResult()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DeviceListUpdateService::class.java)
    }
}