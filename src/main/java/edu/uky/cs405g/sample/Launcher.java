package edu.uky.cs405g.sample;

// With permission from Dr. Bumgardner

import com.google.gson.Gson;
import edu.uky.cs405g.sample.database.DBEngine;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

public class Launcher {

    public static Gson gson;
    public static DBEngine dbEngine;

    public static void main(String[] args) throws IOException {

        gson = new Gson();

        //Database Client initialization
        String DBuser = "jtbi225";
        String DBpassword = "Ktm200exc";
        //for your laptop DBhost = "localhost", for your VM instance DBhost = [your account].cs.uky.edu
        String DBhost = "localhost";
        String DBname = "SMPDB";

        System.out.println("Starting Database...");
        dbEngine = new DBEngine(DBhost, DBname, DBuser, DBpassword);

        //Embedded HTTP initialization
        startServer();

        try {
            while (true) {
                Thread.sleep(5000);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private static void startServer() throws IOException {
        final ResourceConfig rc = new ResourceConfig()
                .packages("edu.uky.cs405g.sample.httpcontrollers");

        System.out.println("Starting Web Server...");
        URI BASE_URI = UriBuilder.fromUri("http://0.0.0.0/")
				.port(9990).build();
        HttpServer httpServer = GrizzlyHttpServerFactory
				.createHttpServer(BASE_URI, rc);

        try {
            httpServer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    } // startServer()

} // class Launcher
