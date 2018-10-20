# Patient Android Application

The Patient Android Application (PAA) implements the User Interface
provided by T3.3 and connects all user interactions with background
webservices.
The app allows the patient to choose a doctor, save in the address book, get and display different vital data, manage and get notifications.

# Packages

All packages will be subpackages of `eu.credential.app.patient`.

For example, all user interface classes will be situated under `eu.credential.app.patient.ui`. The User Interface will communicate with the business/application logic situated under `eu.credential.app.patient.orchestration`. Examples for interfaces provided by the orchetration package are:

* class RegistrationHandler with methods `createAccount`
* class CollectorService with methods `broadcastMeasurementCollected`, `receiveMeasurement`, `receiveDeviceInformation`
* class MyAndroidFirebaseInstanceIdService with methods `onTokenRefresh`, `saveAppId`
* etc.

The User Interface should only communicate with the Orchestrator.

All integration with other services or libraries is done in subpackges within `eu.credential.app.patient.integration`. For example, integration with different bluetooth digital measurers like scale or glucometer will happen in `eu.credential.app.patient.integration.bluetooth`.
Each partner's work will be focuces upon a single package.

# Development Environment

* Minimum supported Android Version: 6.0
* AIT will use the free AndroidStudio IDE and recommends for partners to use the same