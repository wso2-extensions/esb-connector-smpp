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
package org.wso2.carbon.esb.connector;

import org.apache.synapse.MessageContext;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;

import java.io.IOException;
import java.util.Date;

/**
 * Send SMS message.
 */
public class SendSMS extends AbstractConnector implements Connector {
    /**
     * @param messageContext The message context that is processed by a handler in the handle method
     * @throws ConnectException
     */
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        SMPPSession session;
        SMSDto dto = new SMSDto();
        //Indicates SMS application service
        dto.setServiceType((String) getParameter(messageContext, SMPPConstants.SERVICE_TYPE));
        //Type of number for source address
        dto.setSourceAddressTon((String) getParameter(messageContext,
                SMPPConstants.SOURCE_ADDRESS_TON));
        //Numbering plan indicator for source address
        dto.setSourceAddressNpi((String) getParameter(messageContext,
                SMPPConstants.SOURCE_ADDRESS_NPI));
        //Source address of the short message
        String sourceAddress = (String) getParameter(messageContext,
                SMPPConstants.SOURCE_ADDRESS);
        //Type of number for destination
        dto.setDistinationAddressTon((String) getParameter(messageContext,
                SMPPConstants.DISTINATION_ADDRESS_TON));
        //Numbering plan indicator for destination
        dto.setDistinationAddressNpi((String) getParameter(messageContext,
                SMPPConstants.DISTINATION_ADDRESS_NPI));
        //Destination address of the short message
        String distinationAddress = (String) getParameter(messageContext,
                SMPPConstants.DISTINATION_ADDRESS);
        //Used to define message mode and message type
        dto.setEsmclass((String) getParameter(messageContext, SMPPConstants.ESM_CLASS));
        //protocol identifier
        dto.setProtocolid((String) getParameter(messageContext, SMPPConstants.PROTOCOL_ID));
        //sets the priority of the message
        dto.setPriorityflag((String) getParameter(messageContext, SMPPConstants.PRIORITY_FLAG));
        //Delivery of the message
        TimeFormatter timeFormatter = new AbsoluteTimeFormatter();
        //validity period of message
        String validityPeriod = (String) getParameter(messageContext, SMPPConstants.VALIDITY_PERIOD);
        //Type of the SMSC delivery receipt
        dto.setSmscDeliveryReceipt((String) getParameter(messageContext,
                SMPPConstants.SMSC_DELIVERY_RECEIPT));
        //flag indicating if submitted message should replace an existing message
        dto.setReplaceIfPresentFlag((String) getParameter(messageContext,
                SMPPConstants.REPLACE_IF_PRESENT_FLAG));
        //Alphabet used in the data encoding of the message
        dto.setAlphabet((String) getParameter(messageContext, SMPPConstants.ALPHABET));
        dto.setMessageClass((String) getParameter(messageContext, SMPPConstants.MESSAGE_CLASS));
        dto.setCompressed((String) getParameter(messageContext, SMPPConstants.IS_COMPRESSED));
        //Defines the encoding scheme of the SMS message
        GeneralDataCoding dataCoding = new GeneralDataCoding(Alphabet.valueOf(dto.getAlphabet()),
                MessageClass.valueOf(dto.getMessageClass()), dto.isCompressed());
        //indicates short message to send from a predefined list of messages stored on SMSC
        dto.setSubmitDefaultMsgId((String) getParameter(messageContext,
                SMPPConstants.SUBMIT_DEFAULT_MESSAGE_ID));
        //Content of the SMS
        String message = (String) getParameter(messageContext, SMPPConstants.SMS_MESSAGE);
        //Get the user session from the message context
        session = (SMPPSession) messageContext.getProperty(SMPPConstants.SMPP_SESSION);

        if (log.isDebugEnabled()) {
            log.debug("Start Sending SMS");
        }
        try {
            //Send the SMS message
            String messageId = session.submitShortMessage(
                    dto.getServiceType(),
                    TypeOfNumber.valueOf(dto.getSourceAddressTon()),
                    NumberingPlanIndicator.valueOf(dto.getDistinationAddressNpi()),
                    sourceAddress,
                    TypeOfNumber.valueOf(dto.getDistinationAddressTon()),
                    NumberingPlanIndicator.valueOf(dto.getDistinationAddressNpi()),
                    distinationAddress,
                    new ESMClass(dto.getEsmclass()),
                    (byte) dto.getProtocolid(), (byte) dto.getPriorityflag(),
                    timeFormatter.format(new Date()),
                    validityPeriod,
                    new RegisteredDelivery(SMSCDeliveryReceipt.valueOf(dto.getSmscDeliveryReceipt())),
                    (byte) dto.getReplaceIfPresentFlag(),
                    dataCoding, (byte) dto.getSubmitDefaultMsgId(),
                    message.getBytes());
            if (log.isDebugEnabled()) {
                log.debug("Message submitted, message_id is " + messageId);
            }
        } catch (PDUException e) {
            // Invalid PDU parameter
            handleException("Invalid PDU parameter" + e.getMessage(), e, messageContext);
        } catch (ResponseTimeoutException e) {
            // Response timeout
            handleException("Response timeout" + e.getMessage(), e, messageContext);
        } catch (InvalidResponseException e) {
            // Invalid responselid respose"
            handleException("Invalid response" + e.getMessage(), e, messageContext);
        } catch (NegativeResponseException e) {
            // Receiving negative response (non-zero command_status)
            handleException("Receive negative response" + e.getMessage(), e, messageContext);
        } catch (IOException e) {
            handleException("IO error occur" + e.getMessage(), e, messageContext);
        } catch (Exception e) {
            handleException("IO error occur" + e.getMessage(), e, messageContext);
        }
    }
}