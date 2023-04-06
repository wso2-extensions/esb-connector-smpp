/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import org.apache.synapse.MessageContext;
import org.wso2.carbon.esb.connector.exceptions.ConfigurationException;

public class SMPPUtils {

    /**
     * Returns the session name associated with the provided MessageContext object.
     *
     * @param messageContext MessageContext
     * @return Session name
     * @throws ConfigurationException if the session name is not set
     */
    public static String getSessionName(MessageContext messageContext) throws ConfigurationException {

        String connectionName = (String) messageContext.getProperty(SMPPConstants.SESSION_NAME);
        if (connectionName == null) {
            throw new ConfigurationException("The session name property is not set.");
        }
        return connectionName;
    }

    /**
     * Returns the maximum number of characters allowed in a SMPP message for the specified alphabet.
     *
     * @param alphabet the alphabet of the message
     * @return the maximum number of characters allowed in a message for the specified alphabet.
     */
    public static int getSMPPMaxCharacterLength(String alphabet) {
        if (SMPPConstants.ALPHA_USC2.equals(alphabet)) {
            return SMPPConstants.MAX_MULTIPART_MSG_SEGMENT_SIZE_USC2;
        } else {
            return SMPPConstants.MAX_MULTIPART_MSG_SEGMENT_SIZE_DEFAULT;
        }
    }
}
