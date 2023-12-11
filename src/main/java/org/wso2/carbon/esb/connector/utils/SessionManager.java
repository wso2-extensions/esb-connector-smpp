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
package org.wso2.carbon.esb.connector.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.wso2.carbon.esb.connector.store.SessionsStore;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Session Manager maintains the bind connection with smsc for reuse until a unbind is triggered.
 */
public class SessionManager {

    protected Log log = LogFactory.getLog(this.getClass());
    private static SessionManager sessionManager;

    // Map for locks for each key
    private ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public static synchronized SessionManager getInstance() {
        if (sessionManager == null)
            sessionManager = new SessionManager();
        return sessionManager;
    }

    /**
     * @param enquireLinkTimer Enquire Link Timer for bind properties.
     * @param transactionTimer Transaction  Timer for bind properties.
     * @param host host name or ip of the SMSC.
     * @param port connection port of the SMSC.
     * @param bindParameter the list of parameter needed for the SMSC connectivuty.
     * @param retryCount retry count for unbound sessions.
     * @param sessionName sessionName to identify the sessions.
     * @throws IOException
     */
    public SMPPSession getSmppSession(int enquireLinkTimer, int transactionTimer, String host, int port,
                                      BindParameter bindParameter, int retryCount, String sessionName)
            throws IOException {
        return getSmppSession(enquireLinkTimer, transactionTimer, host, port, bindParameter, retryCount, sessionName,
                              60000);
    }

    /**
     * @param enquireLinkTimer Enquire Link Timer for bind properties.
     * @param transactionTimer Transaction  Timer for bind properties.
     * @param host host name or ip of the SMSC.
     * @param port connection port of the SMSC.
     * @param bindParameter the list of parameter needed for the SMSC connectivuty.
     * @param retryCount retry count for unbound sessions.
     * @param sessionName sessionName to identify the sessions.
     * @param sessionBindTimeout session bind timeout.
     * @throws IOException
     */
    public SMPPSession getSmppSession(int enquireLinkTimer, int transactionTimer, String host, int port,
                                      BindParameter bindParameter, int retryCount, String sessionName
            , long sessionBindTimeout)
            throws IOException {
        SMPPSession session = SessionsStore.getSMPPSession(sessionName);
        // If the session is not available or not bound, create a new session and bind in a synchronized manner.
        if (session == null) {
            ReentrantLock lock = lockMap.computeIfAbsent(sessionName, k -> new ReentrantLock());
            lock.lock();
            try {
                session = SessionsStore.getSMPPSession(sessionName);
                if (session == null) {
                    session = new SMPPSession();
                    session.setEnquireLinkTimer(enquireLinkTimer);
                    session.setTransactionTimer(transactionTimer);
                    session.connectAndBind(host, port, bindParameter, sessionBindTimeout);
                    if (log.isDebugEnabled()) {
                        log.debug("A new session is Connected and bind to host: " + host + ", port: " + port +
                                ", systemId: " + bindParameter.getSystemId() +
                                ", Session ID: " + session.getSessionId());
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        //handle unbound sessions
        if (retryCount > 0 && !session.getSessionState().isBound()) {
            if (log.isDebugEnabled()) {
                log.debug("Session is unbound. Unbinding the session and creating a new one, " +
                        "Session ID: " + session.getSessionId() + ", Session state: " + session.getSessionState());
            }
            unbind(sessionName);
            return getSmppSession(enquireLinkTimer, transactionTimer, host, port, bindParameter, retryCount - 1,
                                  sessionName, sessionBindTimeout);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Returning the session, " + "Session ID:  " +
                        session.getSessionId() + ", Session state: " + session.getSessionState() +
                        " for host: " + host + ", port: " + port + ", systemId: " + bindParameter.getSystemId());
            }
            return session;
        }
    }

    /**
     * @param sessionName sessionName to identify the sessions
     */
    public void unbind(String sessionName) {
        SMPPSession session = SessionsStore.getSMPPSession(sessionName);
        if (session != null) {
            if (log.isDebugEnabled()) {
                log.debug("Unbinding the connection with Session ID: " + session.getSessionId());
            }
            session.unbindAndClose();
        } else {
            log.info("No active smpp session found for unbinding for sessionName: " + sessionName);
        }
    }
}
