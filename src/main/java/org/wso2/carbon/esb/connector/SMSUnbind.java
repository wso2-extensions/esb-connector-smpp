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

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;

public class SMSUnbind extends AbstractConnector implements Connector {
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        //IP address of the SMSC
        String host = (String) getParameter(messageContext, SMPPConstants.HOST);
        //port to access the SMSC
        int port = Integer.parseInt((String) getParameter(messageContext, SMPPConstants.PORT));
        //Identifies the ESME system requesting to bind as a transmitter with the SMSC.
        String systemId = (String) getParameter(messageContext, SMPPConstants.SYSTEM_ID);
        SessionManager.getInstance().unbind(host, port, systemId);
        log.info("SMSC Connection unbind is completed.");
    }
}
