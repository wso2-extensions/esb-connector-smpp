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

public class SessionManager {
    protected Log log = LogFactory.getLog(this.getClass());
    private SMPPSession smppSession;
    private static SessionManager sessionManager;

    private SessionManager () {

    }

    public static SessionManager getInstance(){
        if (sessionManager == null)
            sessionManager = new SessionManager();
        return sessionManager;
    }

    public SMPPSession getSmppSession(int enquireLinkTimer, int transactionTimer, String host, int port,
                                       BindParameter
                                               bindParameter) throws IOException{
        if (smppSession != null) {
            return smppSession;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Genarating new bind session for " + host);
            }
            getNewSmppSession(enquireLinkTimer, transactionTimer, host, port, bindParameter);
            return smppSession;
        }

    }

    public SMPPSession getBindedSmppSession(){
        if (smppSession != null) {
            return smppSession;
        } else {
            log.error("Session init needs to be trigger before sending sms to bind with the SMSC");
            return null;
        }
    }

    public SMPPSession getNewSmppSession(int enquireLinkTimer, int transactionTimer, String host, int port,
                                         BindParameter bindParameter) throws IOException {
        unbind();
        smppSession = new SMPPSession();
        smppSession.setEnquireLinkTimer(enquireLinkTimer);
        smppSession.setTransactionTimer(transactionTimer);
        smppSession.connectAndBind(host,
                port, bindParameter);
        if (log.isDebugEnabled()) {
            log.debug("Conected and bind to " + host);
        }
        return smppSession;
    }

    public void unbind() {
        if (smppSession != null) {
            if (log.isDebugEnabled()) {
                log.debug("Unbinding the connection with ");
            }
            smppSession.unbindAndClose();
        } else {
            log.info("No active smpp session found for unbinding");
        }
    }
}