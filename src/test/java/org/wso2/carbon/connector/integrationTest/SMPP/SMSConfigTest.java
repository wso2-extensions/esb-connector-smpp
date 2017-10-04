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
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.testng.Assert;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.connector.SMPPConstants;
import org.wso2.carbon.esb.connector.SMSConfig;
import org.apache.synapse.mediators.template.TemplateContext;


import static org.mockito.Matchers.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.*;

import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.esb.connector.SendSMS;

import java.util.Stack;

@PrepareForTest({org.wso2.carbon.esb.connector.SMSConfig.class, SendSMS.class})

public class SMSConfigTest {

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
    public void testConnect() throws Exception {
        templateContext.getMappedValues().put(SMPPConstants.HOST, "127.0.0.1");
        templateContext.getMappedValues().put(SMPPConstants.SYSTEM_ID, "test");
        templateContext.getMappedValues().put(SMPPConstants.PASSWORD, "pass");
        templateContext.getMappedValues().put(SMPPConstants.PORT, "2775");
        templateContext.getMappedValues().put(SMPPConstants.ENQUIRELINK_TIMER, "1000");
        templateContext.getMappedValues().put(SMPPConstants.TRANSACTION_TIMER, "80");
        templateContext.getMappedValues().put(SMPPConstants.SYSTEM_TYPE, "UNKNOWN");
        templateContext.getMappedValues().put(SMPPConstants.ADDRESS_TON, "NETWORK_SPECIFIC");
        templateContext.getMappedValues().put(SMPPConstants.ADDRESS_NPI, "INTERNET");
        functionStack.push(templateContext);
        ctx.setProperty("_SYNAPSE_FUNCTION_STACK", functionStack);

        SMPPSession session = PowerMockito.mock(SMPPSession.class);
        BindParameter bindParameter = PowerMockito.mock(BindParameter.class);
        whenNew(SMPPSession.class).withNoArguments().thenReturn(session);
        whenNew(BindParameter.class).withAnyArguments().thenReturn(bindParameter);
        when(session.connectAndBind(anyString(), anyInt(), any(BindParameter.class))).thenReturn("some string");

        smsConfig.connect(ctx);
        Assert.assertEquals(session, ctx.getProperty(SMPPConstants.SMPP_SESSION));
    }

    @Test(expectedExceptions = SynapseException.class)
    public void testConnectNegativeMissingHostValue() throws Exception {
        templateContext.getMappedValues().put(SMPPConstants.PORT, "2775");
        templateContext.getMappedValues().put(SMPPConstants.SYSTEM_ID, "test");
        templateContext.getMappedValues().put(SMPPConstants.PASSWORD, "pass");
        functionStack.push(templateContext);
        ctx.setProperty("_SYNAPSE_FUNCTION_STACK", functionStack);
        smsConfig.connect(ctx);
    }

    @Test(expectedExceptions = SynapseException.class)
    public void testConnectNegativeMissingSystemIdValue() throws Exception {
        templateContext.getMappedValues().put(SMPPConstants.HOST, "127.0.0.1");
        templateContext.getMappedValues().put(SMPPConstants.PORT, "2775");
        templateContext.getMappedValues().put(SMPPConstants.PASSWORD, "pass");
        functionStack.push(templateContext);
        ctx.setProperty("_SYNAPSE_FUNCTION_STACK", functionStack);
        smsConfig.connect(ctx);
    }

    @Test(expectedExceptions = SynapseException.class)
    public void testConnectNegativeMissingPasswordValue() throws Exception {
        templateContext.getMappedValues().put(SMPPConstants.HOST, "127.0.0.1");
        templateContext.getMappedValues().put(SMPPConstants.PORT, "2775");
        templateContext.getMappedValues().put(SMPPConstants.SYSTEM_ID, "test");
        functionStack.push(templateContext);
        ctx.setProperty("_SYNAPSE_FUNCTION_STACK", functionStack);
        smsConfig.connect(ctx);
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