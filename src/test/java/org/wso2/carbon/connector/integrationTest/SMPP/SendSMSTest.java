/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector.integrationTest.SMPP;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.connector.utils.SMPPConstants;
import org.wso2.carbon.esb.connector.operations.SMSConfig;
import org.apache.synapse.mediators.template.TemplateContext;


import static org.mockito.Matchers.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.*;

import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.esb.connector.operations.SendSMS;

import java.util.Stack;

@PrepareForTest({SMSConfig.class, SendSMS.class})

public class SendSMSTest {
    private MessageContext ctx;
    ConfigurationContext configContext;
    SynapseConfiguration synapseConfig;
    SMSConfig smsConfig;
    SendSMS sendSMS;
    private TemplateContext templateContext;
    private Stack functionStack;

    @BeforeMethod
    public void setUp() throws Exception {
        ctx = createMessageContext();
        smsConfig = new SMSConfig();
        sendSMS = new SendSMS();
        templateContext = new TemplateContext("smpp", null);

        functionStack = new Stack();
        initMocks(this);
    }

    @Test
    public void testConnectWithMandatoryParameters() throws Exception {
        setMandatoryProperties(templateContext);
        functionStack.push(templateContext);
        ctx.setProperty("_SYNAPSE_FUNCTION_STACK", functionStack);
        mockSMPP();
        Assert.assertEquals(ctx.getEnvelope().getBody().getFirstElement().getText(), "smsc 1000");
    }

    @Test
    public void testConnectWithOptionalParameters() throws Exception {
        setMandatoryProperties(templateContext);
        setOptionalProperties(templateContext);
        functionStack.push(templateContext);
        ctx.setProperty("_SYNAPSE_FUNCTION_STACK", functionStack);
        mockSMPP();
        Assert.assertEquals(ctx.getEnvelope().getBody().getFirstElement().getText(), "smsc 1000");
    }

    private void setMandatoryProperties(TemplateContext templateContext) {
        templateContext.getMappedValues().put(SMPPConstants.HOST, "127.0.0.1");
        templateContext.getMappedValues().put(SMPPConstants.SYSTEM_ID, "test");
        templateContext.getMappedValues().put(SMPPConstants.PASSWORD, "pass");
        templateContext.getMappedValues().put(SMPPConstants.PORT, "2775");
        templateContext.getMappedValues().put(SMPPConstants.SMS_MESSAGE, "message");
    }

    private void setOptionalProperties(TemplateContext templateContext) {
        templateContext.getMappedValues().put(SMPPConstants.SERVICE_TYPE, "CMT");
        templateContext.getMappedValues().put(SMPPConstants.SOURCE_ADDRESS_TON, "NETWORK_SPECIFIC");
        templateContext.getMappedValues().put(SMPPConstants.SOURCE_ADDRESS_NPI, "INTERNET");
        templateContext.getMappedValues().put(SMPPConstants.SOURCE_ADDRESS, "16116");
        templateContext.getMappedValues().put(SMPPConstants.DESTINATION_ADDRESS_TON, "SUBSCRIBER_NUMBER");
        templateContext.getMappedValues().put(SMPPConstants.DESTINATION_ADDRESS_NPI, "LAND_MOBILE");
        templateContext.getMappedValues().put(SMPPConstants.DESTINATION_ADDRESS, "628176504657");
        templateContext.getMappedValues().put(SMPPConstants.ESM_CLASS, "0");
        templateContext.getMappedValues().put(SMPPConstants.PROTOCOL_ID, "1");
        templateContext.getMappedValues().put(SMPPConstants.PRIORITY_FLAG, "0");
        templateContext.getMappedValues().put(SMPPConstants.VALIDITY_PERIOD, "020610233429000R");
        templateContext.getMappedValues().put(SMPPConstants.SMSC_DELIVERY_RECEIPT, "SUCCESS_FAILURE");
        templateContext.getMappedValues().put(SMPPConstants.REPLACE_IF_PRESENT_FLAG, "0");
        templateContext.getMappedValues().put(SMPPConstants.ALPHABET, "ALPHA_DEFAULT");
        templateContext.getMappedValues().put(SMPPConstants.MESSAGE_CLASS, "CLASS1");
        templateContext.getMappedValues().put(SMPPConstants.IS_COMPRESSED, "true");
        templateContext.getMappedValues().put(SMPPConstants.SUBMIT_DEFAULT_MESSAGE_ID, "1");
    }

    private void mockSMPP() throws Exception {
        SMPPSession session = PowerMockito.mock(SMPPSession.class);
        BindParameter bindParameter = PowerMockito.mock(BindParameter.class);
        whenNew(SMPPSession.class).withNoArguments().thenReturn(session);
        whenNew(BindParameter.class).withAnyArguments().thenReturn(bindParameter);

        when(session.connectAndBind(anyString(), anyInt(), any(BindParameter.class))).thenReturn("some string");
        when(session.submitShortMessage(anyString(), any(TypeOfNumber.class), any(NumberingPlanIndicator.class),
                anyString(), any(TypeOfNumber.class), any(NumberingPlanIndicator.class), anyString(), any(ESMClass.class),
                anyByte(), anyByte(), anyString(), anyString(), any(RegisteredDelivery.class), anyByte(),
                any(DataCoding.class), anyByte(), any(byte[].class)).getMessageId()).thenReturn("smsc 1000");

        smsConfig.connect(ctx);
        sendSMS.connect(ctx);
    }

    /**
     * Create Axis2 Message Context.
     *
     * @return msgCtx created message context.
     * @throws AxisFault
     */
    private MessageContext createMessageContext() throws AxisFault {
        MessageContext msgCtx = createSynapseMessageContext();
        org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) msgCtx).getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());

        return msgCtx;
    }

    /**
     * Create Synapse Context.
     *
     * @return mc created message context.
     * @throws AxisFault
     */
    private MessageContext createSynapseMessageContext() throws AxisFault {
        org.apache.axis2.context.MessageContext axis2MC = new org.apache.axis2.context.MessageContext();
        axis2MC.setConfigurationContext(this.configContext);
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MC.setServiceContext(svcCtx);
        axis2MC.setOperationContext(opCtx);
        Axis2MessageContext mc = new Axis2MessageContext(axis2MC, this.synapseConfig, null);
        mc.setMessageID(UIDGenerator.generateURNString());
        mc.setEnvelope(OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope());
        mc.getEnvelope().addChild(OMAbstractFactory.getSOAP12Factory().createSOAPBody());

        return mc;
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new PowerMockObjectFactory();
    }
}