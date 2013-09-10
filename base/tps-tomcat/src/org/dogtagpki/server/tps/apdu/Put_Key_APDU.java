/* --- BEGIN COPYRIGHT BLOCK ---
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301  USA
 *
 * Copyright (C) 2007 Red Hat, Inc.
 * All rights reserved.
 * --- END COPYRIGHT BLOCK ---
 */
package org.dogtagpki.server.tps.apdu;

import org.dogtagpki.server.tps.main.TPSBuffer;

public class Put_Key_APDU extends APDU {
    /**
     * Constructs Put Key APDU.
     */
    public Put_Key_APDU(byte p1, byte p2, TPSBuffer data)
    {
        SetCLA((byte) 0x84);
        SetINS((byte) 0xd8);
        SetP1(p1);
        SetP2(p2);
        SetData(data);
    }

    public APDU_Type GetType()
    {
        return APDU_Type.APDU_PUT_KEY;
    }

}