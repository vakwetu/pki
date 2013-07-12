//--- BEGIN COPYRIGHT BLOCK ---
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; version 2 of the License.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License along
//with this program; if not, write to the Free Software Foundation, Inc.,
//51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//(C) 2012 Red Hat, Inc.
//All rights reserved.
//--- END COPYRIGHT BLOCK ---
package com.netscape.certsrv.key;

import java.net.URISyntaxException;

import com.netscape.certsrv.client.ClientConfig;
import com.netscape.certsrv.client.PKIClient;
import com.netscape.certsrv.request.RequestId;

/**
 * @author Endi S. Dewata
 */
public class KeyClient {

    public PKIClient client;
    public KeyResource keyClient;
    public KeyRequestResource keyRequestClient;

    public KeyClient(ClientConfig config) throws URISyntaxException {
        this(new PKIClient(config));
    }

    public KeyClient(PKIClient client) throws URISyntaxException {
        this.client = client;
        init();
    }

    public void init() throws URISyntaxException {
        keyClient = client.createProxy(KeyResource.class);
        keyRequestClient = client.createProxy(KeyRequestResource.class);
    }

    public KeyDataInfos findKeys(String clientID, String status, Integer maxSize, Integer maxTime) {
        return keyClient.listKeys(clientID, status, maxSize, maxTime);
    }

    public KeyData retrieveKey(KeyRecoveryRequest data) {
        return keyClient.retrieveKey(data);
    }

    public KeyRequestInfos findKeyRequests(
            String requestState,
            String requestType,
            String clientID,
            RequestId start,
            Integer pageSize,
            Integer maxResults,
            Integer maxTime) {
        return keyRequestClient.listRequests(
                requestState,
                requestType,
                clientID,
                start,
                pageSize,
                maxResults,
                maxTime);
    }
}