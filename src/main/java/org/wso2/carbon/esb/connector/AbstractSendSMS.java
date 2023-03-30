/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.jsmpp.session.SMPPSession;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;
import org.wso2.carbon.esb.connector.exception.ConfigurationException;

import java.text.ParseException;
import java.util.Iterator;

import static org.wso2.carbon.esb.connector.SMPPConstants.SMPP_MAX_CHARACTERS;

/**
 * Parent for SMS Send Operations
 */
public abstract class AbstractSendSMS extends AbstractConnector implements Connector {

    /**
     * Provides the SMS DTO with common attributes for sub classes.
     *
     * @param messageContext synapse message context
     * @return SMS DTO with common attributes
     */
    protected SMSDTO getDTO(MessageContext messageContext) throws ConfigurationException {

        SMSDTO dto = new SMSDTO();
        try {
            //Indicates SMS application service
            dto.setServiceType((String) getParameter(messageContext, SMPPConstants.SERVICE_TYPE));
            //Type of number for source address
            dto.setSourceAddressTon((String) getParameter(messageContext,
                    SMPPConstants.SOURCE_ADDRESS_TON));
            //Numbering plan indicator for source address
            dto.setSourceAddressNpi((String) getParameter(messageContext,
                    SMPPConstants.SOURCE_ADDRESS_NPI));
            //Used to define message mode and message type
            dto.setEsmclass((String) getParameter(messageContext, SMPPConstants.ESM_CLASS));
            //protocol identifier
            dto.setProtocolid((String) getParameter(messageContext, SMPPConstants.PROTOCOL_ID));
            //sets the priority of the message
            dto.setPriorityflag((String) getParameter(messageContext, SMPPConstants.PRIORITY_FLAG));
            //Type of the SMSC delivery receipt
            dto.setSmscDeliveryReceipt((String) getParameter(messageContext,
                    SMPPConstants.SMSC_DELIVERY_RECEIPT));
            //flag indicating if submitted message should replace an existing message
            dto.setReplaceIfPresentFlag((String) getParameter(messageContext,
                    SMPPConstants.REPLACE_IF_PRESENT_FLAG));
            //Alphabet used in the data encoding of the message
            dto.setAlphabet((String) getParameter(messageContext, SMPPConstants.ALPHABET));
            dto.setCharset((String) getParameter(messageContext, SMPPConstants.CHARSET));
            dto.setMessageClass((String) getParameter(messageContext, SMPPConstants.MESSAGE_CLASS));
            dto.setCompressed((String) getParameter(messageContext, SMPPConstants.IS_COMPRESSED));
            //indicates short message to send from a predefined list of messages stored on SMSC
            dto.setSubmitDefaultMsgId((String) getParameter(messageContext,
                    SMPPConstants.SUBMIT_DEFAULT_MESSAGE_ID));
            dto.setMessage((String) getParameter(messageContext, SMPPConstants.SMS_MESSAGE));
            dto.setValidityPeriod((String) getParameter(messageContext, SMPPConstants.VALIDITY_PERIOD));
            dto.setSourceAddress((String) getParameter(messageContext, SMPPConstants.SOURCE_ADDRESS));
            dto.setScheduleDeliveryTime((String) getParameter(messageContext, SMPPConstants.SCHEDULE_DELIVERY_TIME));
        } catch (ParseException e) {
            throw new ConfigurationException(e, "error while parsing schedule delivery time");
        }

        return dto;
    }

    /**
     * Prepare payload is used to delete the element in existing body and add the new element.
     *
     * @param messageContext The message context that is used to prepare payload message flow.
     * @param element        The OMElement that needs to be added in the body.
     */
    protected void preparePayload(MessageContext messageContext, OMElement element) {

        SOAPBody soapBody = messageContext.getEnvelope().getBody();
        for (Iterator itr = soapBody.getChildElements(); itr.hasNext(); ) {
            OMElement child = (OMElement) itr.next();
            child.detach();
        }
        soapBody.addChild(element);
    }

    /**
     * Returns the SMPP session from the message context.
     *
     * @param messageContext synapse message context
     * @return SMPP session
     * @throws ConnectException if the session does not exist
     */
    protected SMPPSession getSession(MessageContext messageContext) throws ConnectException {

        SMPPSession session = (SMPPSession) messageContext.getProperty(SMPPConstants.SMPP_SESSION);
        if (session == null) {
            String msg = "No Active SMPP Connection found to perform the action. Please trigger SMPP.INIT Prior to " +
                    "SendSMS";
            log.error(msg);
            throw new ConnectException(msg);
        }
        return session;
    }

    /**
     * Handles SMPP errors.
     *
     * @param msg Error message
     * @param e Exception
     * @param messageContext synapse message context
     */
    protected void handleSMPPError(String msg, Exception e, MessageContext messageContext) {
        messageContext.setProperty(SMPPConstants.SMPP_ERROR, e.getMessage());
        handleException(msg, e, messageContext);
    }

    /**
     * This method will check whether the message length is greater than maximum SMPP character limit.
     *
     * @param dto The SMS DTO containing all the message related data
     * @return true if the message length is greater than maximum SMPP character limit
     */
    protected boolean isLongSMS(SMSDTO dto) {

        return dto.getMessage().getBytes().length > SMPP_MAX_CHARACTERS;
    }
}
