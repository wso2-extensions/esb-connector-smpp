/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.connector.operations;

import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.wso2.carbon.esb.connector.utils.SMPPConstants;
import org.wso2.carbon.esb.connector.utils.SMPPUtils;
import org.wso2.carbon.esb.connector.utils.SessionManager;
import org.wso2.carbon.esb.connector.store.SessionsStore;
import org.wso2.integration.connector.core.AbstractConnector;
import org.wso2.integration.connector.core.ConnectException;
import org.wso2.integration.connector.core.Connector;

import java.io.IOException;
/**
 * Configure the SMSC
 */
public class SMSConfig extends AbstractConnector implements Connector {
    /**
     * @param messageContext The message context that is processed by a handler in the handle method
     * @throws ConnectException
     */
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String sessionName = SMPPUtils.getSessionName(messageContext);
        SMPPSession session = SessionsStore.getSMPPSession(sessionName);
        if (session != null) {
            if (SessionState.CLOSED.equals(session.getSessionState())) {
                SessionsStore.removeSMPPSession(sessionName);
            } else {
                return;
            }
        }
        //IP address of the SMSC
        String host = (String) getParameter(messageContext, SMPPConstants.HOST);
        //port to access the SMSC
        int port = Integer.parseInt((String) getParameter(messageContext, SMPPConstants.PORT));
        //Identifies the ESME system requesting to bind as a transmitter with the SMSC.
        String systemId = (String) getParameter(messageContext, SMPPConstants.SYSTEM_ID);
        //The password may be used by the SMSC to authenticate the ESME requesting to bind
        String password = (String) getParameter(messageContext, SMPPConstants.PASSWORD);

        if (StringUtils.isEmpty(host) || StringUtils.isEmpty((String) getParameter(messageContext, SMPPConstants.PORT))
                || StringUtils.isEmpty(systemId) || StringUtils.isEmpty(password)) {
            handleException("Missing one or more mandatory bind parameter(s)", messageContext);
        }

        //Used to check whether SMSC is connected or not
        String enquirelinktimer = (String) getParameter(messageContext,
                SMPPConstants.ENQUIRELINK_TIMER);
        int enquireLinkTimer;
        if (StringUtils.isEmpty(enquirelinktimer)) {
            //set it to default value
            enquireLinkTimer = SMPPConstants.ENQUIRELINK_TIMER_DEFAULT;
        } else {
            enquireLinkTimer = Integer.parseInt(enquirelinktimer);
        }
        //Used to set Session Binding Timeout
        String sessionBindingTimeout = (String) getParameter(messageContext,
                SMPPConstants.SESSION_BIND_TIMEOUT);
        long sessionBindingTimeoutValue;
        if (StringUtils.isEmpty(sessionBindingTimeout)) {
            //set it to default value
            sessionBindingTimeoutValue = 60000;
        } else {
            try {
                sessionBindingTimeoutValue = Long.parseLong(sessionBindingTimeout);
            } catch (NumberFormatException e) {
                // Set to default value in case of an exception
                sessionBindingTimeoutValue = 60000;
            }
        }
        //Time elapsed between smpp request and the corresponding response
        String transactiontimer = (String) getParameter(messageContext,
                SMPPConstants.TRANSACTION_TIMER);
        int transactionTimer;
        if (StringUtils.isEmpty(transactiontimer)) {
            //set it to default value
            transactionTimer = SMPPConstants.TRANSACTION_TIMER_DEFAULT;
        } else {
            transactionTimer = Integer.parseInt(transactiontimer);
        }
        //Identifies the type of ESME system requesting to bind as a transmitter with the SMSC
        String systemType = (String) getParameter(messageContext, SMPPConstants.SYSTEM_TYPE);
        if (StringUtils.isEmpty(systemType)) {
            systemType = SMPPConstants.CP;
        }
        //Indicates Type of Number of the ESME address
        String addressTON = (String) getParameter(messageContext, SMPPConstants.ADDRESS_TON);
        if (StringUtils.isEmpty(addressTON)) {
            addressTON = SMPPConstants.UNKNOWN;
        }
        //Numbering Plan Indicator for ESME address
        String addressNPI = (String) getParameter(messageContext, SMPPConstants.ADDRESS_NPI);
        if (StringUtils.isEmpty(addressNPI)) {
            addressNPI = SMPPConstants.UNKNOWN;
        }

        try {
            session = SessionManager.getInstance().getSmppSession(enquireLinkTimer, transactionTimer, host, port,
                    new BindParameter(BindType.BIND_TX, systemId,
                            password, systemType,
                            TypeOfNumber.valueOf(addressTON),
                            NumberingPlanIndicator.valueOf(
                                    addressNPI)
                            , null),
                    SMPPConstants.MAX_RETRY_COUNT, sessionName
                    , sessionBindingTimeoutValue);
            SessionsStore.addSMPPSession(sessionName, session);
            if (log.isDebugEnabled()) {
                log.debug("Connected and bind to " + host);
            }
        } catch (IOException e) {
            handleException("Error while configuring: " + e.getMessage(), e, messageContext);
        }
    }
}