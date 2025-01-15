package Database;
import java.sql.*;

public class Database
{
    //To make it more usable
    private Connection connection;
    private String URL;
    private String username;
    private String password;

    //Step 1: Create the SQL table
    private final String CreateTable =
            "CREATE TABLE `Project` "
            + " (`timestep` VARCHAR(20) NOT NULL DEFAULT '00-00-0000 00:00:00',"
            + "  `device_id` double NOT NULL DEFAULT 0,"
            + "  `latitude` double DEFAULT NULL,"
            + "  `longitude` double DEFAULT NULL,"
            + "  `Smoke` double DEFAULT NULL,"
            + "  `Gas` double DEFAULT NULL,"
            + "  `Temperature` double DEFAULT NULL,"
            + "  `UV` double DEFAULT NULL,"
            + "  `Danger` integer DEFAULT NULL,"   //0 - not in danger, 1- in danger
            + "  PRIMARY KEY (device_id, timestep)"
            + ")";

    //We used the in the beginning for testing purpose
    private final String InsertDummyValues = "INSERT INTO Project VALUES ('31-12-2023 23:59:59',26,NULL,NULL,NULL,NULL,NULL,NULL,NULL);";

    //Step 2: Create a connection
    public Database(String URL, String username, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");     //Register the mysql driver
            connection = DriverManager.getConnection(URL, username, password);
            System.out.println("Successful connecting to the database.");
            this.URL = URL;
            this.username = username;
            this.password = password;
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Failed connecting to the database.");
            System.out.printf("SQLException: %s\n\n", ex.getMessage());
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.out.println("In shutdown hook");
            }
        }, "Shutdown-thread"));
    }

    //Step 3: Initialization
    public void Initialization() {
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet resultSet = metaData.getTables(null, null, "Project", null);

                if (!resultSet.next()) {
                    // Table does not exist, so create it
                    statement.execute(CreateTable);
                    System.out.println("Creating table successful.");
                } else {
                    //Dropping Table
                    String sql = "DROP TABLE Project";
                    statement.executeUpdate(sql);
                    System.out.println("Table is deleted.");
                    statement.execute(CreateTable);
                }
                resultSet.close();
            } catch (SQLException ex) {
                System.out.println("Failed initializing the database.");
                System.out.printf("SQLException: %s\n\n", ex.getMessage());
            }
        } else {
            System.out.println("There is no connection to the database in order to initialize it.");
        }
    }

    //Step 4: Insert to the database
    public void InsertDB(String timestep, double device_id, double latitude, double longitude, double Smoke, double Gas, double Temperature, double UV, double Danger) {
        try {
            connection = DriverManager.getConnection(URL, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Project (timestep, device_id, latitude, longitude, Smoke, Gas, Temperature, UV, Danger) VALUES(?,?,?,?,?,?,?,?,?)");

            preparedStatement.setString(1, timestep);
            preparedStatement.setDouble(2, device_id);
            preparedStatement.setDouble(3, latitude);
            preparedStatement.setDouble(4, longitude);
            if (Smoke == 0.0 && Gas == 0.0 && Temperature == 0.0 && UV == 0.0) {    //Initially all of them have not detected any danger
                preparedStatement.setNull(5, Types.DOUBLE);
                preparedStatement.setNull(6, Types.DOUBLE);
                preparedStatement.setNull(7, Types.DOUBLE);
                preparedStatement.setNull(8, Types.DOUBLE);
            } else {
                preparedStatement.setDouble(5, Smoke);
                preparedStatement.setDouble(6, Gas);
                preparedStatement.setDouble(7, Temperature);
                preparedStatement.setDouble(8, UV);
            }
            preparedStatement.setDouble(9, Danger);

            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("Failed inserting to the database.");
            System.out.printf("SQLException: %s\n\n", ex.getMessage());
        }
    }


    //Step 5: Print the elements of the database
    public void PrintDB() throws SQLException {
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery("SELECT * from mydb.Values");  //Used to execute SELECT query - select all data from my database
        final ResultSetMetaData rsmd = resultSet.getMetaData(); //Saving result into a tep var
        final int columnsNumber = rsmd.getColumnCount();
        System.out.println("---------------------DB---------------------");
        while (resultSet.next()) {
            for (int i = 1; i <= columnsNumber; ++i) {
                if (i > 1) {
                    System.out.print(",  ");
                }
                final String columnValue = resultSet.getString(i);
                System.out.println(columnValue + " " + rsmd.getColumnName(i));
            }
            System.out.println("");
        }
    }
}
