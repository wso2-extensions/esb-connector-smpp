/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.connector.store;

import org.jsmpp.session.SMPPSession;

import java.util.concurrent.ConcurrentHashMap;

public class SessionsStore {

    private static final ConcurrentHashMap<String, SMPPSession> sessionsMap = new ConcurrentHashMap<>();

    /**
     * Adds an SMPP session to the sessions map with the specified name.
     *
     * @param sessionsName Name to be assigned to the SMPP session
     * @param session      SMPPSession to be added
     * @return true if the SMPP session was added successfully, false if the session already exists in the map
     */
    public static boolean addSMPPSession(String sessionsName, SMPPSession session) {
        if (!sessionsMap.containsKey(session)) {
            sessionsMap.put(sessionsName, session);
            return true;
        }
        return false;
    }

    /**
     * Removes an SMPP session with the specified name from the sessions map.
     *
     * @param sessionName the name of the SMPP session to be removed
     * @return true if the SMPP session was removed successfully, false if the session name does not exist in the map
     */
    public static boolean removeSMPPSession(String sessionName) {
        if (sessionName.contains(sessionName)) {
            sessionsMap.remove(sessionName);
            return true;
        }
        return false;
    }

    /**
     * Returns an SMPP session with the specified name from the sessions map.
     *
     * @param sessionName the name of the SMPP session to be retrieved
     */
    public static SMPPSession getSMPPSession(String sessionName) {
        if (sessionName.contains(sessionName)) {
            return sessionsMap.get(sessionName);
        }
        return null;
    }

}
