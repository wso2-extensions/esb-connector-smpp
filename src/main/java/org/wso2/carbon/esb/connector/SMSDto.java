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

import org.apache.commons.lang.StringUtils;

public class SMSDto {
    private String serviceType;
    private String sourceAddressTon;
    private String sourceAddressNpi;
    private String distinationAddressTon;
    private String distinationAddressNpi;
    private int esmclass;
    private int protocolid;
    private int priorityflag;
    private String smscDeliveryReceipt;
    private int replaceIfPresentFlag;
    private String alphabet;
    private String messageClass;
    private boolean isCompressed;
    private int submitDefaultMsgId;

    public String getDistinationAddressTon() {
        return distinationAddressTon;
    }

    public void setDistinationAddressTon(String distinationAddressTon) {
        if (StringUtils.isEmpty(distinationAddressTon)) {
            distinationAddressTon = SMPPConstants.UNKNOWN;
        }
        this.distinationAddressTon = distinationAddressTon;
    }

    public String getDistinationAddressNpi() {
        return distinationAddressNpi;
    }

    public void setDistinationAddressNpi(String distinationAddressNpi) {
        if (StringUtils.isEmpty(distinationAddressNpi)) {
            distinationAddressNpi = SMPPConstants.UNKNOWN;
        }
        this.distinationAddressNpi = distinationAddressNpi;
    }

    public int getEsmclass() {
        return esmclass;
    }

    public void setEsmclass(String esmClass) {
        if (StringUtils.isEmpty(esmClass)) {
            //set it to default value
            esmclass = 0;
        } else {
            esmclass = Integer.parseInt(esmClass);
        }
    }

    public int getProtocolid() {
        return protocolid;
    }

    public void setProtocolid(String protocolId) {
        if (StringUtils.isEmpty(protocolId)) {
            //set it to default value
            protocolid = 0;
        } else {
            protocolid = Integer.parseInt(protocolId);
        }
    }

    public int getPriorityflag() {
        return priorityflag;
    }

    public void setPriorityflag(String priorityFlag) {
        if (StringUtils.isEmpty(priorityFlag)) {
            //set it to default value
            priorityflag = 1;
        } else {
            priorityflag = Integer.parseInt(priorityFlag);
        }
    }

    public String getSmscDeliveryReceipt() {
        return smscDeliveryReceipt;
    }

    public void setSmscDeliveryReceipt(String smscDeliveryReceipt) {
        if (StringUtils.isEmpty(smscDeliveryReceipt)) {
            smscDeliveryReceipt = SMPPConstants.DEFAULT;
        }
        this.smscDeliveryReceipt = smscDeliveryReceipt;
    }

    public int getReplaceIfPresentFlag() {
        return replaceIfPresentFlag;
    }

    public void setReplaceIfPresentFlag(String replaceifpresentflag) {
        if (StringUtils.isEmpty(replaceifpresentflag)) {
            //set it to default value
            replaceIfPresentFlag = 0;
        } else {
            replaceIfPresentFlag = Integer.parseInt(replaceifpresentflag);
        }
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        if (StringUtils.isEmpty(alphabet)) {
            alphabet = SMPPConstants.ALPHA_DEFAULT;
        }
        this.alphabet = alphabet;
    }

    public String getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(String messageClass) {
        if (StringUtils.isEmpty(messageClass)) {
            messageClass = SMPPConstants.CLASS1;
        }
        this.messageClass = messageClass;
    }

    public boolean isCompressed() {
        return isCompressed;
    }

    public void setCompressed(String iscompressed) {
        if (StringUtils.isEmpty(iscompressed)) {
            //set it to default value
            isCompressed = false;
        } else {
            isCompressed = Boolean.parseBoolean(iscompressed);
        }
    }

    public int getSubmitDefaultMsgId() {
        return submitDefaultMsgId;
    }

    public void setSubmitDefaultMsgId(String submitdefaultmsgid) {
        if (StringUtils.isEmpty(submitdefaultmsgid)) {
            //set it to default value
            submitDefaultMsgId = 0;
        } else {
            submitDefaultMsgId = Integer.parseInt(submitdefaultmsgid);
        }
    }

    public String getSourceAddressNpi() {
        return sourceAddressNpi;
    }

    public void setSourceAddressNpi(String sourceAddressNpi) {
        if (StringUtils.isEmpty(sourceAddressNpi)) {
            sourceAddressNpi = SMPPConstants.UNKNOWN;
        }
        this.sourceAddressNpi = sourceAddressNpi;
    }

    public String getSourceAddressTon() {

        return sourceAddressTon;
    }

    public void setSourceAddressTon(String sourceAddressTon) {
        if (StringUtils.isEmpty(sourceAddressTon)) {
            sourceAddressTon = SMPPConstants.UNKNOWN;
        }
        this.sourceAddressTon = sourceAddressTon;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        if (StringUtils.isEmpty(serviceType)) {
            serviceType = SMPPConstants.CMT;
        }
        this.serviceType = serviceType;
    }
}
