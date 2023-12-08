/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.esb.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Session Manager maintains the bind connection with smsc for reuse until a unbind is triggered.
 */
public class SessionManager {

    protected Log log = LogFactory.getLog(this.getClass());
    private Map<String, SMPPSession> smppSessionList;
    private static SessionManager sessionManager;

    private SessionManager() {
        smppSessionList = new HashMap();
    }

    // Map for locks for each key
    private ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    public static synchronized SessionManager getInstance() {
        if (sessionManager == null)
            sessionManager = new SessionManager();
        return sessionManager;
    }

    private String getKey(String host, int port, String systemId) {
        return host + SMPPConstants.CONCT_CHAR + port + SMPPConstants.CONCT_CHAR + systemId;

    }
    /**
     * @param enquireLinkTimer Enquire Link Timer for bind properties.
     * @param transactionTimer Transaction  Timer for bind properties.
     * @param host host name or ip of the SMSC.
     * @param port connection port of the SMSC.
     * @param bindParameter the list of parameter needed for the SMSC connectivuty.
     * @param retryCount retry count for unbound sessions.
     * @throws IOException
     */
    public SMPPSession getSmppSession(int enquireLinkTimer, int transactionTimer, String host, int port,
                                      BindParameter bindParameter, int retryCount) throws IOException {
        return getSmppSession(enquireLinkTimer, transactionTimer, host, port, bindParameter, retryCount
                , 60000);
    }

    /**
     * @param enquireLinkTimer Enquire Link Timer for bind properties.
     * @param transactionTimer Transaction  Timer for bind properties.
     * @param host host name or ip of the SMSC.
     * @param port connection port of the SMSC.
     * @param bindParameter the list of parameter needed for the SMSC connectivuty.
     * @param retryCount retry count for unbound sessions.
     * @param sessionBindingTimeout session binding timeout.
     * @throws IOException
     */
    public SMPPSession getSmppSession(int enquireLinkTimer, int transactionTimer, String host, int port,
                                      BindParameter bindParameter, int retryCount,
                                      long sessionBindingTimeout) throws IOException {
        String key = getKey(host, port, bindParameter.getSystemId());
        SMPPSession smppSession = smppSessionList.get(key);
        // If the session is not available or not bound, create a new session and bind in a synchronized manner.
        if (smppSession == null) {
            ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
            lock.lock();
            try {
                smppSession = smppSessionList.get(key);
                if (smppSession == null) {
                    smppSession = new SMPPSession();
                    smppSession.setEnquireLinkTimer(enquireLinkTimer);
                    smppSession.setTransactionTimer(transactionTimer);
                    smppSession.connectAndBind(host, port, bindParameter, sessionBindingTimeout);
                    if (log.isDebugEnabled()) {
                        log.debug("A new session is Connected and bind to host: " + host + ", port: " + port +
                                ", systemId: " + bindParameter.getSystemId() +
                                ", Session ID: " + smppSession.getSessionId());
                    }
                    smppSessionList.putIfAbsent(key, smppSession);
                }

            } finally {
                lock.unlock();
            }
        }
        //handle unbound sessions
        if (retryCount > 0 && !smppSession.getSessionState().isBound()) {
            if (log.isDebugEnabled()) {
                log.debug("Session is unbound. Unbinding the session and creating a new one, " +
                        "Session ID: " + smppSession.getSessionId() + ", Session state: " + smppSession.getSessionState());
            }
            unbind(host, port, bindParameter.getSystemId());
            return getSmppSession(enquireLinkTimer, transactionTimer, host, port, bindParameter
                    , retryCount - 1, sessionBindingTimeout);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Returning the session, " + "Session ID:  " +
                        smppSession.getSessionId() + ", Session state: " + smppSession.getSessionState() +
                        " for host: " + host + ", port: " + port + ", systemId: " + bindParameter.getSystemId());
            }
            return smppSession;
        }
    }

    /**
     * @param host host name or ip of the SMSC.
     * @param port connection port of the SMSC.
     * @param systemId systemID used to bind the connection to SMSC.
     */
    public void unbind(String host, int port, String systemId) {
        SMPPSession smppSession = smppSessionList.get(getKey(host, port, systemId));
        if (smppSession != null) {
            if (log.isDebugEnabled()) {
                log.debug("Unbinding the connection with SMSC host: " + host + " port: " + port +
                        "Session ID: " + smppSession.getSessionId());
            }
            smppSession.unbindAndClose();
            smppSessionList.remove(getKey(host, port, systemId));
        } else {
            log.info("No active smpp session found for unbinding for host: " + host + " port: "
                    + port + " systemId: " + systemId);
        }
    }
}
