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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.NavController
import li.klass.fhem.R
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.xmllist.XmlListDevice

abstract class ActionCardButton(buttonText: Int, context: Context) : ActionCardAction {

    private val buttonText: String = context.getString(buttonText)

    override fun createView(device: XmlListDevice, connectionId: String?, context: Context, inflater: LayoutInflater, parent: ViewGroup, navController: NavController): View {
        val button = inflater.inflate(R.layout.button_device_detail, parent, false) as Button
        button.text = buttonText
        button.setOnClickListener { this@ActionCardButton.onClick(device, connectionId, context, navController) }

        return button
    }

    protected abstract fun onClick(device: XmlListDevice, connectionId: String?, context: Context, navController: NavController)

    override fun supports(device: FhemDevice): Boolean = true
}
