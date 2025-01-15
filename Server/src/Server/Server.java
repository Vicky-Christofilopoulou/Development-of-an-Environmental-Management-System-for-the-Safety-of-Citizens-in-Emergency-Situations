package Server;

import Database.Database;
import MQTT.MqttHandler;
import Maps.Maps;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


public class Server {
    final MqttHandler mqttController;
    final Database dbController;
    Maps maps;
    public Server(String mqttBrokerUrl, String databaseUrl, String username, String password) throws IOException, SAXException, ParserConfigurationException, InterruptedException {
        this.dbController = new Database(databaseUrl, username, password);
        System.out.println(databaseUrl);

        dbController.Initialization();
        System.out.println("Initialization");

        System.out.println(mqttBrokerUrl);
        this.mqttController = new MqttHandler(mqttBrokerUrl, dbController);
        System.out.println("mqttController");

        this.maps = new Maps();
    }

    public void initializeMqttConnections()
    {
        mqttController.Connect();
    }
}