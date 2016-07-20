
#SMPP ESB Connector

SMPP Connector allows you to send SMS through the WSO2 ESB. It uses jsmpp API to communicate with a SMSC (Short Message service center) which is to store, forward, convert and deliver Short Message Service (SMS) messages. jsmpp is a java implementation of SMPP protocol. The SMPP server in SMSC have all ESME (External Short Messaging Entity) addresses which is an external application that connects to a SMSC and the active connection. When you send SMS to destination, it comes to the SMSC, then one of the module in SMSC checks the destination address whether it is available or not. If it is available it create connection object that is responsible for sending SMS message.

#Build

mvn clean install


#How You Can Contribute

You can create a third party connector and publish in WSO2 Store.

https://docs.wso2.com/display/ESBCONNECTORS/Creating+a+Third+Party+Connector+and+Publishing+in+WSO2+Store

