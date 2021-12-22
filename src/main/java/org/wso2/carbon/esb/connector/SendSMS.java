/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.*;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SMPPSession;
import org.wso2.carbon.connector.core.ConnectException;

import java.io.IOException;

/**
 * Send SMS message.
 */
public class SendSMS extends AbstractSendSMS {

    /**
     * @param messageContext The message context that is processed by a handler in the handle method
     * @throws ConnectException
     */
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {

        if (log.isDebugEnabled()) {
            log.debug("Start Sending SMS");
        }
        try {
            SMPPSession session = getSession(messageContext);
            SMSDTO dto = getDTO(messageContext);
            //Defines the encoding scheme of the SMS message
            GeneralDataCoding dataCoding = new GeneralDataCoding(Alphabet.valueOf(dto.getAlphabet()),
                    MessageClass.valueOf(dto.getMessageClass()), dto.isCompressed());
            //Type of number for destination
            dto.setDistinationAddressTon((String) getParameter(messageContext,
                    SMPPConstants.DISTINATION_ADDRESS_TON));
            //Numbering plan indicator for destination
            dto.setDistinationAddressNpi((String) getParameter(messageContext,
                    SMPPConstants.DISTINATION_ADDRESS_NPI));
            //Destination address of the short message
            String distinationAddress = (String) getParameter(messageContext,
                    SMPPConstants.DISTINATION_ADDRESS);
            //Send the SMS message
            String messageId = session.submitShortMessage(
                    dto.getServiceType(),
                    TypeOfNumber.valueOf(dto.getSourceAddressTon()),
                    NumberingPlanIndicator.valueOf(dto.getSourceAddressNpi()),
                    dto.getSourceAddress(),
                    TypeOfNumber.valueOf(dto.getDistinationAddressTon()),
                    NumberingPlanIndicator.valueOf(dto.getDistinationAddressNpi()),
                    distinationAddress,
                    new ESMClass(dto.getEsmclass()),
                    (byte) dto.getProtocolid(), (byte) dto.getPriorityflag(),
                    dto.getScheduleDeliveryTime(),
                    dto.getValidityPeriod(),
                    new RegisteredDelivery(SMSCDeliveryReceipt.valueOf(dto.getSmscDeliveryReceipt())),
                    (byte) dto.getReplaceIfPresentFlag(),
                    dataCoding, (byte) dto.getSubmitDefaultMsgId(),
                    dto.getMessage().getBytes());

            generateResult(messageContext, messageId);

            if (log.isDebugEnabled()) {
                log.debug("Message submitted, message_id is " + messageId);
            }
        } catch (PDUException e) {
            // Invalid PDU parameter
            handleSMPPError("Invalid PDU parameter" + e.getMessage(), e, messageContext);
        } catch (ResponseTimeoutException e) {
            // Response timeout
            handleSMPPError("Response timeout" + e.getMessage(), e, messageContext);
        } catch (InvalidResponseException e) {
            // Invalid response
            handleSMPPError("Invalid response" + e.getMessage(), e, messageContext);
        } catch (NegativeResponseException e) {
            // Receiving negative response (non-zero command_status)
            handleSMPPError("Receive negative response" + e.getMessage(), e, messageContext);
        } catch (IOException e) {
            handleSMPPError("IO error occur" + e.getMessage(), e, messageContext);
        } catch (Exception e) {
            handleSMPPError("Unexpected error occur" + e.getMessage(), e, messageContext);
        }
    }

    /**
     * Generate the result is used to display the result(messageId) after sending message is complete.
     *
     * @param messageContext The message context that is used in generate result mediation flow.
     * @param resultStatus   Boolean value of the result to display.
     */
    private void generateResult(MessageContext messageContext, String resultStatus) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace(SMPPConstants.SMPPCON, SMPPConstants.NAMESPACE);
        OMElement messageElement = factory.createOMElement(SMPPConstants.MESSAGE_ID, ns);
        messageElement.setText(resultStatus);
        preparePayload(messageContext, messageElement);
    }
}
