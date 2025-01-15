# Development-of-an-Environmental-Management-System-for-the-Safety-of-Citizens-in-Emergency-Situations
The present work aims to develop an advanced Environmental Management System that focuses on citizen safety during emergency situations. The developed system provides a comprehensive framework for the collection, analysis, and management of data related to environmental conditions and safety.

![system_architecture](https://github.com/user-attachments/assets/192a36fa-4eef-4240-bd90-89960055bbae)

For the implementation of this project, an Android application and a server were created, communicating via the MQTT protocol. Specifically, the system operates using Android devices, which, connected via the MQTT protocol, exchange information with the Edge Server.

The Edge Server is responsible for collecting, analyzing, and exchanging data between the Android devices and static IoT devices placed in the environment. These IoT devices gather measurements through embedded sensors.

Additionally, the Edge Server performs analysis of the data from the IoT devices to identify potential risks. In the event of detecting a risk, it notifies the Android devices and logs the identified threats into a MySQL database.






## Contributors of the project :
* [Zannis Vidalis](https://github.com/ZannisVidalis)
* [Eleni Feslian](https://github.com/EleniFeslian)
* [Konstantinos Sgouras](https://github.com/KonosSgouras)
* [Vicky Christofilopoulou]( https://github.com/Vicky-Christofilopoulou )
