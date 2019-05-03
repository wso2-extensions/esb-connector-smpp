
#SMPP ESB Connector

SMPP  [Connector](https://docs.wso2.com/display/EI650/Working+with+Connectors) allows you to send SMS through the WSO2 ESB. It uses jsmpp API to communicate with a SMSC (Short Message service center) which is to store, forward, convert and deliver Short Message Service (SMS) messages. jsmpp is a java implementation of SMPP protocol. The SMPP server in SMSC have all ESME (External Short Messaging Entity) addresses which is an external application that connects to a SMSC and the active connection. When you send SMS to destination, it comes to the SMSC, then one of the module in SMSC checks the destination address whether it is available or not. If it is available it create connection object that is responsible for sending SMS message.

## Compatibility

| Connector version | Supported SMPP JDK version | Supported WSO2 ESB/EI version |
| ------------- | ---------------|------------- |
| [1.0.2](https://github.com/wso2-extensions/esb-connector-smpp/tree/org.wso2.carbon.connector.SMPP-1.0.2) | jsmpp-2.1.0-RELEASE | ESB 4.9.0, EI 6.2.0, EI 6.3.0, EI 6.4.0, EI 6.5.0  |
| [1.0.1](https://github.com/wso2-extensions/esb-connector-smpp/tree/org.wso2.carbon.esb.connector.SMPP-1.0.1) | jsmpp-2.1.0-RELEASE | ESB 4.9.0, ESB 5.0.0, EI 6.2.0   |
| [1.0.0](https://github.com/wso2-extensions/esb-connector-smpp/tree/org.wso2.carbon.esb.connector.SMPP-1.0.0) | jsmpp-2.1.0-RELEASE | ESB 4.9.0, ESB 5.0.0   |

#### Download and install the connector

1. Download the connector from the [WSO2 Store](https://store.wso2.com/store/assets/esbconnector/details/3fcaf309-1a69-4edf-870a-882bb76fdaa1) by clicking the **Download Connector** button.
2. You can then follow this [documentation](https://docs.wso2.com/display/EI650/Working+with+Connectors+via+the+Management+Console) to add the connector to your WSO2 EI instance and to enable it (via the management console).
3. For more information on using connectors and their operations in your WSO2 EI configurations, see [Using a Connector](https://docs.wso2.com/display/EI650/Using+a+Connector).
4. If you want to work with connectors via WSO2 EI Tooling, see [Working with Connectors via Tooling](https://docs.wso2.com/display/EI650/Working+with+Connectors+via+Tooling).

## Building from the source

Follow the steps given below to build the SMPP connector from the source code.

1. Get a clone or download the source from [Github](https://github.com/wso2-extensions/esb-connector-smpp).
2. Run the following Maven command from the `esb-connector-smpp` directory: `mvn clean install`.
3. The ZIP file with the SMPP connector is created in the `esb-connector-smpp/target` directory.

## How You Can Contribute

As an open source project, WSO2 extensions welcome contributions from the community.
Check the [issue tracker](https://github.com/wso2-extensions/esb-connector-amazondynamodb/issues) for open issues that interest you. We look forward to receiving your contributions.


