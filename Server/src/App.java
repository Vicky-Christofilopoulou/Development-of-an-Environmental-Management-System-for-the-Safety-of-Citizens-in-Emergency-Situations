import Server.Server;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class App {

    private static final String mqttBrokerUrl = "tcp://192.168.211.157:2028";
    private static final String databaseUrl = "jdbc:mysql://localhost:3306/mysql";
    private static final String databaseUsername = "root";
    private static final String databasePassword = "Vasiliki2002";  //vicky

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, InterruptedException {
        App app = new App();
        Server server = new Server(mqttBrokerUrl, databaseUrl, databaseUsername, databasePassword);
        server.initializeMqttConnections();
    }
}