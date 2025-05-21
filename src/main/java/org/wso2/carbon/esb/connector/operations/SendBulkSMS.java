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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.Address;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.ReplaceIfPresentFlag;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SubmitMultiResult;
import org.wso2.carbon.esb.connector.utils.SMPPConstants;
import org.wso2.carbon.esb.connector.utils.SMPPUtils;
import org.wso2.carbon.esb.connector.dto.SMSDTO;
import org.wso2.carbon.esb.connector.exceptions.ConfigurationException;
import org.wso2.carbon.esb.connector.store.SessionsStore;
import org.wso2.integration.connector.core.ConnectException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.wso2.carbon.esb.connector.utils.SMPPConstants.UDHIE_HEADER_LENGTH;
import static org.wso2.carbon.esb.connector.utils.SMPPConstants.UDHIE_IDENTIFIER_SAR;
import static org.wso2.carbon.esb.connector.utils.SMPPConstants.UDHIE_SAR_LENGTH;

public class SendBulkSMS extends AbstractSendSMS {

    @Override
    public void execute(MessageContext messageContext, String responseVariable, Boolean overwriteBody)
            throws ConnectException {

        if (log.isDebugEnabled()) {
            log.debug("Start Sending Bulk SMS");
        }
        try {
            SMSDTO dto = getDTO(messageContext);
            String sessionName = SMPPUtils.getSessionName(messageContext);
            SMPPSession session = SessionsStore.getSMPPSession(sessionName);
            //Defines the encoding scheme of the SMS message
            GeneralDataCoding dataCoding = new GeneralDataCoding(Alphabet.valueOf(dto.getAlphabet()),
                    MessageClass.valueOf(dto.getMessageClass()), dto.isCompressed());
            //Destination addresses payload
            Object addresses = getMediatorParameter(messageContext, SMPPConstants.DESTINATION_ADDRESSES,
                    String.class, false);
            byte[] messageBytes = dto.getMessage().getBytes(dto.getCharset());
            if (isLongSMS(dto)) {

                int maxMultipartMessageSegmentSize = SMPPUtils.getSMPPMaxCharacterLength(dto.getAlphabet());
                List<SubmitMultiResult> multiResultList = new ArrayList<>();

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
                    SubmitMultiResult multiResult = submitMultipleMessages(session, dto, dataCoding, addresses,
                                                                           msgSegment);

                    if (log.isDebugEnabled()) {
                        log.info("MessageId of segment " + segmentID + " : " + multiResult.getMessageId());
                    }
                    multiResultList.add(multiResult);
                    start += size;
                }
                generateBulkResult(messageContext, multiResultList, responseVariable, overwriteBody, true);
            } else {
                if(dto.getEsmclass() == SMPPConstants.ESM_CLASS_NOT_SET) {
                    dto.setEsmclass(SMPPConstants.ESM_CLASS_DEFAULT);
                }
                SubmitMultiResult multiResult = submitMultipleMessages(session, dto, dataCoding, addresses,
                                                                       messageBytes);
                List<SubmitMultiResult> resultList = new ArrayList<>();
                resultList.add(multiResult);
                generateBulkResult(messageContext, resultList, responseVariable, overwriteBody, false);
            }
        } catch (ConfigurationException e) {
            handleSMPPError("Invalid configuration " + e.getMessage(), e, messageContext);
        } catch (ResponseTimeoutException e) {
            handleSMPPError("Response timeout " + e.getMessage(), e, messageContext);
        } catch (PDUException e) {
            handleSMPPError("Invalid PDU parameter " + e.getMessage(), e, messageContext);
        } catch (IOException e) {
            handleSMPPError("IO error occur " + e.getMessage(), e, messageContext);
        } catch (InvalidResponseException e) {
            handleSMPPError("Invalid response " + e.getMessage(), e, messageContext);
        } catch (NegativeResponseException e) {
            handleSMPPError("Receive negative response " + e.getMessage(), e, messageContext);
        } catch (Exception e) {
            handleSMPPError("Unexpected error occurred " + e.getMessage(), e, messageContext);
        }
    }

    private SubmitMultiResult submitMultipleMessages(SMPPSession session, SMSDTO dto, GeneralDataCoding dataCoding,
                                                     Object addresses, byte[] message) throws Exception{
        return session.submitMultiple(
                dto.getServiceType(),
                TypeOfNumber.valueOf(dto.getSourceAddressTon()),
                NumberingPlanIndicator.valueOf(dto.getSourceAddressNpi()),
                dto.getSourceAddress(),
                generateDestinationAddresses(addresses),
                new ESMClass(dto.getEsmclass()),
                (byte) dto.getProtocolid(),
                (byte) dto.getPriorityflag(),
                dto.getScheduleDeliveryTime(),
                dto.getValidityPeriod(),
                new RegisteredDelivery(SMSCDeliveryReceipt.valueOf(dto.getSmscDeliveryReceipt())),
                new ReplaceIfPresentFlag(dto.getReplaceIfPresentFlag()),
                dataCoding,
                (byte) dto.getSubmitDefaultMsgId(),
                message);
    }

    /**
     * Return an array of addresses corresponding to the destinationAddresses parameter.
     *
     * @param addresses Object of destinationAddresses parameter
     * @return Array of Addresses
     * @throws Exception
     */
    private Address[] generateDestinationAddresses(Object addresses) throws Exception {

        ArrayList<Address> addressList = new ArrayList<>();
        String jsonString;
        JsonParser jsonParser = new JsonParser();
        JsonObject addressObject;
        if (addresses instanceof OMElement) {
            // xpath evaluations include the parent element, which needs to be removed
            jsonString = JsonUtil.toJsonString((OMElement) addresses).toString();
            JsonElement addressElement = jsonParser.parse(jsonString);
            String wrapper = (String) addressElement.getAsJsonObject().keySet().toArray()[0];
            addressObject = addressElement.getAsJsonObject().get(wrapper).getAsJsonObject();
        } else {
            jsonString = (String) addresses;
            JsonElement addressElement = jsonParser.parse(jsonString);
            addressObject = addressElement.getAsJsonObject();
        }

        String numberingPlan = SMPPConstants.UNKNOWN;
        String numberType = SMPPConstants.UNKNOWN;

        if (addressObject.has(SMPPConstants.DESTINATION_ADDRESS_NUMBERING_PLAN)) {
            numberingPlan = addressObject.get(SMPPConstants.DESTINATION_ADDRESS_NUMBERING_PLAN).getAsString();
        }

        if (addressObject.has(SMPPConstants.DESTINATION_ADDRESS_TYPE)) {
            numberType = addressObject.get(SMPPConstants.DESTINATION_ADDRESS_TYPE).getAsString();
        }

        JsonArray mobileNumbers = addressObject.get(SMPPConstants.DESTINATION_ADDRESS_MOBILE_NUMBERS).getAsJsonArray();

        //TODO: If jsmpp library is upgraded, reset this value accordingly.
        if (mobileNumbers.size() > SMPPConstants.MAXIMUM_DESTINATIONS) {
            throw new ConnectException("Number of destinations is invalid. Should not exceed 255.");
        }

        Iterator<JsonElement> numberIterator = mobileNumbers.iterator();
        while (numberIterator.hasNext()) {
            // struct 1, 2 will only execute the else block
            // struct 3 can execute both if and ele blocks
            String number;

            JsonElement numberElement = numberIterator.next();
            if (numberElement.isJsonObject()) {
                String customType = numberType;
                String customNumberPlan = numberingPlan;
                if (numberElement.getAsJsonObject().has(SMPPConstants.DESTINATION_ADDRESS_NUMBERING_PLAN)) {
                    customNumberPlan = numberElement.getAsJsonObject().get(SMPPConstants.DESTINATION_ADDRESS_NUMBERING_PLAN).getAsString();
                }

                if (numberElement.getAsJsonObject().has(SMPPConstants.DESTINATION_ADDRESS_TYPE)) {
                    customType = numberElement.getAsJsonObject().get(SMPPConstants.DESTINATION_ADDRESS_TYPE).getAsString();
                }
                number = numberElement.getAsJsonObject().get(SMPPConstants.DESTINATION_ADDRESS_MOBILE_NUMBER).getAsString();
                addressList.add(new Address(TypeOfNumber.valueOf(customType), NumberingPlanIndicator.valueOf(customNumberPlan), number));
            } else {
                number = numberElement.getAsString();
                addressList.add(new Address(TypeOfNumber.valueOf(numberType), NumberingPlanIndicator.valueOf(numberingPlan), number));
            }
        }
        Address[] addressesArr = new Address[mobileNumbers.size()];
        return addressList.toArray(addressesArr);
    }

    /**
     * Create a unified response payload for both regular and long SMS messages.
     *
     * @param messageContext    Synapse message context
     * @param smrList           List of SubmitMultiResult objects (single item for regular SMS)
     * @param responseVariable  Name of the variable to store the response
     * @param overwriteBody     Flag to indicate whether to overwrite the message body
     * @param isLongSMS         Flag to indicate if this is a long SMS with multiple segments
     */
    private void generateBulkResult(MessageContext messageContext, List<SubmitMultiResult> smrList,
                                           String responseVariable, Boolean overwriteBody, boolean isLongSMS) {
        JsonObject jsonObject = new JsonObject();

        // Add a flag to indicate if the message was sent as a long SMS
        jsonObject.addProperty("isLongSMS", isLongSMS);

        // Create a results array for all message segments
        JsonArray resultsArray = new JsonArray();

        for (SubmitMultiResult smr : smrList) {
            JsonObject resultObject = new JsonObject();
            resultObject.addProperty(SMPPConstants.MESSAGE_ID, smr.getMessageId());

            JsonArray unsuccessfulDeliveriesArray = new JsonArray();

            for (int i = 0; i < smr.getUnsuccessDeliveries().length; i++) {
                JsonObject unsuccessfulDelivery = new JsonObject();
                unsuccessfulDelivery.addProperty(SMPPConstants.DESTINATION_ADDRESS,
                        smr.getUnsuccessDeliveries()[i].getDestinationAddress().getAddress());
                unsuccessfulDelivery.addProperty(SMPPConstants.ERROR_STATUS_CODE,
                        String.valueOf(smr.getUnsuccessDeliveries()[i]));
                unsuccessfulDeliveriesArray.add(unsuccessfulDelivery);
            }

            resultObject.add(SMPPConstants.UNSUCCESSFUL_DELIVERIES, unsuccessfulDeliveriesArray);
            resultsArray.add(resultObject);
        }

        jsonObject.add(SMPPConstants.RESULTS, resultsArray);

        // Add total segment count
        jsonObject.addProperty("segmentCount", smrList.size());

        handleConnectorResponse(messageContext, responseVariable, overwriteBody, jsonObject, null, null);
    }
}
