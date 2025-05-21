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
package org.wso2.carbon.esb.connector.operations;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.esb.connector.dto.SMSDTO;
import org.wso2.carbon.esb.connector.exceptions.ConfigurationException;
import org.wso2.carbon.esb.connector.utils.SMPPConstants;
import org.wso2.carbon.esb.connector.utils.SMPPUtils;
import org.wso2.integration.connector.core.AbstractConnectorOperation;
import org.wso2.integration.connector.core.Connector;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * Parent for SMS Send Operations
 */
public abstract class AbstractSendSMS extends AbstractConnectorOperation implements Connector {

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
            dto.setServiceType(getMediatorParameter(messageContext, SMPPConstants.SERVICE_TYPE, String.class, true));
            //Type of number for source address
            dto.setSourceAddressTon(getMediatorParameter(messageContext,
                    SMPPConstants.SOURCE_ADDRESS_TON, String.class, true));
            //Numbering plan indicator for source address
            dto.setSourceAddressNpi(getMediatorParameter(messageContext,
                    SMPPConstants.SOURCE_ADDRESS_NPI, String.class, true));
            //Used to define message mode and message type
            dto.setEsmclass(getMediatorParameter(messageContext, SMPPConstants.ESM_CLASS, String.class, true));
            //protocol identifier
            dto.setProtocolid(getMediatorParameter(messageContext, SMPPConstants.PROTOCOL_ID, String.class, true));
            //sets the priority of the message
            dto.setPriorityflag(getMediatorParameter(messageContext, SMPPConstants.PRIORITY_FLAG, String.class, true));
            //Type of the SMSC delivery receipt
            dto.setSmscDeliveryReceipt(getMediatorParameter(messageContext,
                    SMPPConstants.SMSC_DELIVERY_RECEIPT, String.class, true));
            //flag indicating if submitted message should replace an existing message
            dto.setReplaceIfPresentFlag(getMediatorParameter(messageContext,
                    SMPPConstants.REPLACE_IF_PRESENT_FLAG, String.class, true));
            //Alphabet used in the data encoding of the message
            dto.setAlphabet(getMediatorParameter(messageContext, SMPPConstants.ALPHABET, String.class, true));
            dto.setCharset(getMediatorParameter(messageContext, SMPPConstants.CHARSET, String.class, true));
            dto.setMessageClass(getMediatorParameter(messageContext, SMPPConstants.MESSAGE_CLASS, String.class, true));
            dto.setCompressed(getMediatorParameter(messageContext, SMPPConstants.IS_COMPRESSED, String.class, true));
            //indicates short message to send from a predefined list of messages stored on SMSC
            dto.setSubmitDefaultMsgId(getMediatorParameter(messageContext,
                    SMPPConstants.SUBMIT_DEFAULT_MESSAGE_ID, String.class, true));
            dto.setMessage(getMediatorParameter(messageContext, SMPPConstants.SMS_MESSAGE, String.class, true));
            dto.setValidityPeriod(
                    getMediatorParameter(messageContext, SMPPConstants.VALIDITY_PERIOD, String.class, true));
            dto.setSourceAddress(
                    getMediatorParameter(messageContext, SMPPConstants.SOURCE_ADDRESS, String.class, true));
            dto.setScheduleDeliveryTime(
                    getMediatorParameter(messageContext, SMPPConstants.SCHEDULE_DELIVERY_TIME, String.class, true));
        } catch (ParseException e) {
            throw new ConfigurationException(e, "error while parsing schedule delivery time");
        }

        return dto;
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
    protected boolean isLongSMS(SMSDTO dto) throws UnsupportedEncodingException {

        return dto.getMessage().getBytes(dto.getCharset()).length >
                SMPPUtils.getSMPPMaxCharacterLength(dto.getAlphabet());
    }
}
