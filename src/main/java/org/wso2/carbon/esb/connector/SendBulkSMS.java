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
import org.jsmpp.bean.SubmitMultiResult;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SMPPSession;
import org.wso2.carbon.connector.core.ConnectException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class SendBulkSMS extends AbstractSendSMS {

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {

        if (log.isDebugEnabled()) {
            log.debug("Start Sending Bulk SMS");
        }
        try {
            SMSDTO dto = getDTO(messageContext);
            SMPPSession session = getSession(messageContext);
            //Defines the encoding scheme of the SMS message
            GeneralDataCoding dataCoding = new GeneralDataCoding(Alphabet.valueOf(dto.getAlphabet()),
                    MessageClass.valueOf(dto.getMessageClass()), dto.isCompressed());
            //Destination addresses payload
            Object addresses = getParameter(messageContext, SMPPConstants.DESTINATION_ADDRESSES);

            SubmitMultiResult multiResult = session.submitMultiple(
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
                    dto.getMessage().getBytes());
            generateBulkResult(messageContext, multiResult);
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
     * Create the response payload using the SubmitMultiResult.
     *
     * @param messageContext Synapse message context
     * @param smr            SubmitMultiResult of the bulk message request
     */
    private void generateBulkResult(MessageContext messageContext, SubmitMultiResult smr) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace(SMPPConstants.SMPPCON, SMPPConstants.NAMESPACE);
        OMElement messageElement = factory.createOMElement(SMPPConstants.MESSAGE_ID, ns);
        messageElement.setText(smr.getMessageId());

        OMElement root = factory.createOMElement(SMPPConstants.RESULTS, ns);
        OMElement unsuccessfulDeliveries = factory.createOMElement(SMPPConstants.UNSUCCESSFUL_DELIVERIES, ns);

        for (int i = 1; i < smr.getUnsuccessDeliveries().length; i++) {
            OMElement destinationAddress = factory.createOMElement(SMPPConstants.DESTINATION_ADDRESS, ns);
            destinationAddress.setText(smr.getUnsuccessDeliveries()[i].getDestinationAddress().getAddress());

            OMElement errorStatusCode = factory.createOMElement(SMPPConstants.ERROR_STATUS_CODE, ns);
            errorStatusCode.setText(String.valueOf(smr.getUnsuccessDeliveries()[i]));

            OMElement unsuccessfulDelivery = factory.createOMElement(SMPPConstants.UNSUCCESSFUL_DELIVERY, ns);
            unsuccessfulDelivery.addChild(destinationAddress);
            unsuccessfulDelivery.addChild(errorStatusCode);

            unsuccessfulDeliveries.addChild(unsuccessfulDelivery);
        }

        root.addChild(messageElement);
        root.addChild(unsuccessfulDeliveries);

        preparePayload(messageContext, root);
    }
}
