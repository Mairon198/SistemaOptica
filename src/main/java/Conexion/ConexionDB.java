
package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConexionDB{
    private static final Properties CONFIG = new Properties();

static {
    try (FileInputStream in = new FileInputStream("config.properties")) {
        CONFIG.load(in);
    } catch (IOException e) {
        throw new RuntimeException("No se encontró config.properties en la carpeta del proyecto", e);
    }
}

private static final String URL = CONFIG.getProperty("db.url");
private static final String USER = CONFIG.getProperty("db.user");
private static final String PASSWORD = CONFIG.getProperty("db.password");

    private static Connection conexion = null;
    private ConexionDB(){}

    public static Connection getConexion() {
        try{
            if(conexion == null || conexion.isClosed()){
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                   //Establecer la conexión
                   conexion = DriverManager.getConnection(URL,USER,PASSWORD);
                   System.out.println("->Conexion existosa a Sql");
            }
        }catch(ClassNotFoundException e){
            System.err.println("Error no se encontro el Driver de mssql-jdbc" + e.getMessage());
        }catch(SQLException e){
            System.err.println("Error al conectar con SQL" + e.getMessage());
        }
        return conexion;
    }

    public static void cerrarConexion(){
        try{
            if(conexion != null && !conexion.isClosed()){
                conexion.close();
                System.out.println("->La conexion con SQL fue cerrada");
            }
        }catch(SQLException e){
            System.err.println("Error al cerrar la conexion" + e.getMessage());
        }
    }
}
