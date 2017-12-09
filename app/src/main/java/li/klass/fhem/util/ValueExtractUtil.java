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

package li.klass.fhem.util;

import com.google.common.base.Strings;

public class ValueExtractUtil {
    public static double extractLeadingDouble(String text) {
        return extractLeadingDouble(text, -1);
    }

    public static float extractLeadingFloat(String text) {
        return (float) extractLeadingDouble(text, -1);
    }

    public static double extractLeadingDouble(String text, int digits) {
        text = extractLeadingNumericText(text, digits);
        if (Strings.isNullOrEmpty(text)) return 0;
        return Double.valueOf(text);
    }

    public static int extractLeadingInt(String text) {
        double value = extractLeadingDouble(text, 0);
        return (int) value;
    }

    static String extractLeadingNumericText(String text, int digits) {
        if (Strings.isNullOrEmpty(text)) return "";
        String numericText = new LeadingNumericTextExtractor(text).numericText();

        if (digits > 0) {
            double number = Double.valueOf(numericText);
            double roundFactor = Math.pow(10, digits);
            double rounded = ((int) (number * roundFactor)) / roundFactor;

            return rounded + "";
        } else {
            return numericText;
        }
    }

    public static boolean onOffToTrueFalse(String value) {
        return value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true");
    }
}
