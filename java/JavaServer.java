// java/JavaServer.java
// A minimal HTTP server using com.sun.net.httpserver.HttpServer.
// Receives POST /javaprocess and returns a documentation stub.

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class JavaServer {
    public static void main(String[] args) throws Exception {
        int port = 8005;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/javaprocess", (exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Received at JavaServer: " + body);

            // Pretend we generate a documentation stub:
            String doc = "{\"status\":\"ok\",\"docId\":\"DOC-" + System.currentTimeMillis() + "\"}";

            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200, doc.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(doc.getBytes());
            os.close();
        }));
        System.out.println("JavaServer listening on port " + port);
        server.setExecutor(null);
        server.start();
    }
}
