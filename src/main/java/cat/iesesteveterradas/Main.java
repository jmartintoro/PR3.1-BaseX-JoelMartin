package cat.iesesteveterradas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

import org.basex.api.client.ClientSession;
import org.basex.core.*;
import org.basex.core.cmd.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);    

    public static void main(String[] args) throws IOException {
         // Initialize connection details
        String host = "127.0.0.1";
        int port = 1984;
        String username = "admin"; // Default username
        String password = "admin"; // Default password

        File directory = new File("./data/inputs");
        File[] files = directory.listFiles();
        List<String> queries = new ArrayList<>();
        
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        StringBuilder content = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        queries.add(content.toString());
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + file.getName() + ". Skipping...");
                    }
                    System.out.println("File readed: " + file.getName());
                }
            }
        }

        // Establish a connection to the BaseX server
        try (ClientSession session = new ClientSession(host, port, username, password)) {
            System.out.println("Connected to BaseX server.");
            session.execute(new Open("sports.meta.stackexchange"));

            int queryIndex = 1;
            for (String query : queries) {
                try {
                    String result = session.execute(new XQuery(query));
                    File outputFile = new File("./data/outputs/result_" + queryIndex + ".xml");
                    try (FileWriter writer = new FileWriter(outputFile)) {
                        writer.write(result);
                    }
                    System.out.println("Query " + queryIndex + " done");
                } catch (BaseXException e) {
                    logger.error("Error executing the query: " + e.getMessage());
                }
                queryIndex++;
            }

        } catch (BaseXException e) {
            logger.error("Error connecting to BaseX server: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error occurred: " + e.getMessage());
        }     
    }
}
