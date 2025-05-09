
#SMPP ESB Connector

SMPP  [Connector](https://docs.wso2.com/display/EI650/Working+with+Connectors) allows you to send SMS through the WSO2 ESB. It uses jsmpp API to communicate with a SMSC (Short Message service center) which is to store, forward, convert and deliver Short Message Service (SMS) messages. jsmpp is a java implementation of SMPP protocol. The SMPP server in SMSC have all ESME (External Short Messaging Entity) addresses which is an external application that connects to a SMSC and the active connection. When you send SMS to destination, it comes to the SMSC, then one of the module in SMSC checks the destination address whether it is available or not. If it is available it create connection object that is responsible for sending SMS message.

## Compatibility

For compatibility information, see the [WSO2 Connector Store](https://store.wso2.com).

#### Use the connector

Please follow the instructions on the [WSO2 documentation](https://mi.docs.wso2.com/en/latest/reference/connectors/smpp-connector/smpp-connector-overview/) to use the connector.

## Build from the source

Follow the steps given below to build the SMPP connector from the source code.

1. Get a clone or download the source from [Github](https://github.com/wso2-extensions/esb-connector-smpp).
2. Run the following Maven command from the `esb-connector-smpp` directory: `mvn clean install`.
3. The ZIP file with the SMPP connector is created in the `esb-connector-smpp/target` directory.

## How You Can Contribute

As an open source project, WSO2 extensions welcome contributions from the community.
Check the [issue tracker](https://github.com/wso2-extensions/esb-connector-smpp/issues) for open issues that interest you. We look forward to receiving your contributions.


