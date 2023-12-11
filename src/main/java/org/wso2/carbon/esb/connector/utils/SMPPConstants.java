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
package org.wso2.carbon.esb.connector.utils;

import java.nio.charset.Charset;

/**
 * Contains all constants used in SMPP connector implementation
 */
public class SMPPConstants {
    // SMPP Config constants
    public static final String CONCT_CHAR = "-";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String SYSTEM_ID = "systemId";
    public static final String PASSWORD = "password";
    public static final String SYSTEM_TYPE = "systemType";
    public static final String ADDRESS_TON = "addressTon";
    public static final String ADDRESS_NPI = "addressNpi";
    public static final String ENQUIRELINK_TIMER = "enquireLinkTimer";
    public static final String SESSION_BIND_TIMEOUT = "sessionBindTimeout";
    public static final int ENQUIRELINK_TIMER_DEFAULT = 50000;
    public static final String TRANSACTION_TIMER = "transactionTimer";
    public static final int TRANSACTION_TIMER_DEFAULT = 100;
    public static final String SMPP_SESSION = "smpp_session";
    public static final String SESSION_NAME = "name";

    //SMPP send SMS Constants
    public static final String SERVICE_TYPE = "serviceType";
    public static final String SOURCE_ADDRESS_TON = "sourceAddressTon";
    public static final String SOURCE_ADDRESS_NPI = "sourceAddressNpi";
    public static final String SOURCE_ADDRESS = "sourceAddress";
    public static final String SCHEDULE_DELIVERY_TIME = "scheduleDeliveryTime";
    public static final String DESTINATION_ADDRESS_TON = "destinationAddressTon";
    public static final String DESTINATION_ADDRESS_NPI = "destinationAddressNpi";
    public static final String ALPHABET = "alphabet";
    public static final String CHARSET = "charset";
    public static final String CHARSET_DEFAULT = Charset.defaultCharset().displayName();
    public static final String SMS_MESSAGE = "message";
    public static final String SMSC_DELIVERY_RECEIPT = "smscDeliveryReceipt";
    public static final String MESSAGE_CLASS = "messageClass";
    public static final String IS_COMPRESSED = "isCompressed";
    public static final String ESM_CLASS = "esmclass";
    public static final String PROTOCOL_ID = "protocolid";
    public static final String PRIORITY_FLAG = "priorityflag";
    public static final String REPLACE_IF_PRESENT_FLAG = "replaceIfPresentFlag";
    public static final String SUBMIT_DEFAULT_MESSAGE_ID = "submitDefaultMsgId";
    public static final String VALIDITY_PERIOD = "validityPeriod";
    public static final String CP = "cp";
    public static final String CMT = "CMT";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String DEFAULT = "DEFAULT";
    public static final String ALPHA_DEFAULT = "ALPHA_DEFAULT";
    public static final String ALPHA_USC2 = "ALPHA_UCS2";
    public static final String CLASS1 = "CLASS1";
    public static final String NAMESPACE = "ns";
    public static final String SMPPCON = "http://org.wso2.esbconnectors.smppConnector";
    public static final String MESSAGE_ID = "messageId";
    public static final String DESTINATION_ADDRESSES = "destinationAddresses";
    public static final String DESTINATION_ADDRESS = "destinationAddress";
    public static final String ERROR_STATUS_CODE = "errorStatusCode";
    public static final String DESTINATION_ADDRESS_MOBILE_NUMBERS = "mobileNumbers";
    public static final String DESTINATION_ADDRESS_TYPE = "type";
    public static final String DESTINATION_ADDRESS_NUMBERING_PLAN= "numberingPlan";
    public static final String DESTINATION_ADDRESS_MOBILE_NUMBER= "mobileNumber";
    public static final String RESULTS = "results";
    public static final String RESULT = "result";
    public static final String UNSUCCESSFUL_DELIVERIES = "unsuccessfulDeliveries";
    public static final String UNSUCCESSFUL_DELIVERY = "unsuccessfulDelivery";
    public static final int MAXIMUM_DESTINATIONS = 255;
    public static final String SMPP_ERROR = "SMPP_ERROR";
    public static final int MAX_RETRY_COUNT = 3;
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    public static final int MAX_MULTIPART_MSG_SEGMENT_SIZE_DEFAULT = 153;
    public static final int MAX_MULTIPART_MSG_SEGMENT_SIZE_USC2 = 133;

    // Length of the rest of the UDH
    public static final byte UDHIE_HEADER_LENGTH = 0x05;
    // UDH to indicate a multipart message.
    public static final byte UDHIE_IDENTIFIER_SAR = 0x00;
    // Length of the sub header(the rest of the UDH)
    public static final byte UDHIE_SAR_LENGTH = 0x03;
    public static final int ESM_CLASS_DEFAULT = 0;
    public static final int ESM_CLASS_HIDE_UDH = 64;
    public static final int ESM_CLASS_NOT_SET = -1;
}
