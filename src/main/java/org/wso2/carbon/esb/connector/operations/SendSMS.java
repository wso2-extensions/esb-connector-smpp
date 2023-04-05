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
package org.wso2.carbon.esb.connector.operations;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.MessageContext;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SMPPSession;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.esb.connector.utils.SMPPConstants;
import org.wso2.carbon.esb.connector.utils.SMPPUtils;
import org.wso2.carbon.esb.connector.dto.SMSDTO;
import org.wso2.carbon.esb.connector.exceptions.ConfigurationException;
import org.wso2.carbon.esb.connector.store.SessionsStore;

import java.io.IOException;
import java.util.Random;

import static org.wso2.carbon.esb.connector.utils.SMPPConstants.UDHIE_HEADER_LENGTH;
import static org.wso2.carbon.esb.connector.utils.SMPPConstants.UDHIE_IDENTIFIER_SAR;
import static org.wso2.carbon.esb.connector.utils.SMPPConstants.UDHIE_SAR_LENGTH;

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
            SMSDTO dto = getDTO(messageContext);
            String sessionName = SMPPUtils.getSessionName(messageContext);
            SMPPSession session = SessionsStore.getSMPPSession(sessionName);

            //Defines the encoding scheme of the SMS message
            GeneralDataCoding dataCoding = new GeneralDataCoding(Alphabet.valueOf(dto.getAlphabet()),
                    MessageClass.valueOf(dto.getMessageClass()), dto.isCompressed());
            //Type of number for destination
            dto.setDestinationAddressTon((String) getParameter(messageContext,
                                                               SMPPConstants.DESTINATION_ADDRESS_TON));
            //Numbering plan indicator for destination
            dto.setDestinationAddressNpi((String) getParameter(messageContext,
                                                               SMPPConstants.DESTINATION_ADDRESS_NPI));
            //Destination address of the short message
            String destinationAddress = (String) getParameter(messageContext,
                    SMPPConstants.DESTINATION_ADDRESS);
            //Send the SMS message
            String messageId = submitMessage(session, dto, dataCoding, destinationAddress);

            generateResult(messageContext, messageId);

            if (log.isDebugEnabled()) {
                log.debug("Message submitted, message_id is " + messageId);
            }
        } catch (ConfigurationException e) {
            handleSMPPError("Invalid configuration " + e.getMessage(), e, messageContext);
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
     * This message will submit the message using the SMPPSession. If the message is long, it will be split into
     * segments and send them as Multipart Messages using User data headers.
     *
     * @param session            The SMPPSession associated with the configs
     * @param dto                The SMS DTO containing all the message related data
     * @param dataCoding         Defines the encoding scheme of the SMS message
     * @param destinationAddress The Destination address the SMS should sent
     * @return String containing the message IDs of the sent SMS
     */
    private String submitMessage(SMPPSession session, SMSDTO dto, GeneralDataCoding dataCoding,
                                 String destinationAddress) throws Exception {

        StringBuilder messageIdList = new StringBuilder();

        byte[] messageBytes = dto.getMessage().getBytes(dto.getCharset());
        if (isLongSMS(dto)) {

            int maxMultipartMessageSegmentSize = SMPPUtils.getSMPPMaxCharacterLength(dto.getAlphabet());
            int remainingByteCount = messageBytes.length % maxMultipartMessageSegmentSize;

            int segments = remainingByteCount > 0 ?
                    messageBytes.length / maxMultipartMessageSegmentSize + 1 :
                    messageBytes.length / maxMultipartMessageSegmentSize;

            int start = 0;
            int size = maxMultipartMessageSegmentSize;

            // generate new reference number
            byte[] referenceNumber = new byte[1];
            new Random().nextBytes(referenceNumber);

            for (int segmentID = 1; segmentID <= segments; segmentID++) {
                if (start + size > messageBytes.length) {
                    size = remainingByteCount;
                }

                byte[] msgSegment = new byte[6 + size];
                // UDH header
                // doesn't include itself, its header length
                msgSegment[0] = UDHIE_HEADER_LENGTH;
                // SAR identifier
                msgSegment[1] = UDHIE_IDENTIFIER_SAR;
                // SAR length
                msgSegment[2] = UDHIE_SAR_LENGTH;
                // reference number (same for all messages)
                msgSegment[3] = referenceNumber[0];
                // total number of segments
                msgSegment[4] = (byte) segments;
                // segment number
                msgSegment[5] = (byte) segmentID;

                // copy the data into the array
                System.arraycopy(messageBytes, start, msgSegment, 6, size);

                if (log.isDebugEnabled()) {
                    log.info("Message Size of segment " + segmentID + " : " + msgSegment.length);
                }
                if(dto.getEsmclass() == SMPPConstants.ESM_CLASS_NOT_SET) {
                    dto.setEsmclass(SMPPConstants.ESM_CLASS_HIDE_UDH);
                }
                String messageId =
                        submitShortMessage(session, dto, dataCoding, destinationAddress, msgSegment);

                if (log.isDebugEnabled()) {
                    log.info("MessageId of segment " + segmentID + " : " + messageId);
                }

                messageIdList.append(messageId);
                if (segmentID < segments) {
                    messageIdList.append(", ");
                }
                start += size;
            }
        } else {
            if(dto.getEsmclass() == SMPPConstants.ESM_CLASS_NOT_SET) {
                dto.setEsmclass(SMPPConstants.ESM_CLASS_DEFAULT);
            }
            String messageId = submitShortMessage(session, dto, dataCoding, destinationAddress, messageBytes);
            messageIdList.append(messageId);
        }
        return messageIdList.toString();
    }

    private static String submitShortMessage(SMPPSession session, SMSDTO dto, GeneralDataCoding dataCoding,
                                             String destinationAddress, byte[] message)
            throws Exception {

        return session.submitShortMessage(
                dto.getServiceType(),
                TypeOfNumber.valueOf(dto.getSourceAddressTon()),
                NumberingPlanIndicator.valueOf(dto.getSourceAddressNpi()),
                dto.getSourceAddress(),
                TypeOfNumber.valueOf(dto.getDestinationAddressTon()),
                NumberingPlanIndicator.valueOf(dto.getDestinationAddressNpi()),
                destinationAddress,
                new ESMClass(dto.getEsmclass()),
                (byte) dto.getProtocolid(), (byte) dto.getPriorityflag(),
                dto.getScheduleDeliveryTime(),
                dto.getValidityPeriod(),
                new RegisteredDelivery(SMSCDeliveryReceipt.valueOf(dto.getSmscDeliveryReceipt())),
                (byte) dto.getReplaceIfPresentFlag(),
                dataCoding, (byte) dto.getSubmitDefaultMsgId(),
                message).getMessageId();
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
