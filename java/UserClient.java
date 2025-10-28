// java/UserClient.java
// JDK-only (uses java.net.HttpURLConnection).
// Sends a user validation request to Python AuthService, then sends the token to Go AnalyzeService,
// and finally POSTs a documentation stub to the local Java server.

import java.io.*;
import java.net.*;

public class UserClient {
    public static void main(String[] args) throws Exception {
        String user = "manan";
        String authUrl = "http://localhost:8001/auth?user=" + URLEncoder.encode(user, "UTF-8");
        String token = httpGet(authUrl).trim();
        System.out.println("Auth token: " + token);

        String analyzeUrl = "http://localhost:8003/analyze";
        String analyzeResponse = httpPostJson(analyzeUrl, "{\"token\":\"" + token + "\",\"user\":\"" + user + "\"}");
        System.out.println("Analyze response: " + analyzeResponse);

        String javaServerUrl = "http://localhost:8005/javaprocess";
        String docPayload = "{\"user\":\"" + user + "\",\"analysis\":" + analyzeResponse + "}";
        String finalResp = httpPostJson(javaServerUrl, docPayload);
        System.out.println("Java server response: " + finalResp);
    }

    static String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3000);
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line, out="";
        while((line=br.readLine())!=null) out += line;
        br.close();
        return out;
    }

    static String httpPostJson(String urlStr, String json) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json");
        try(OutputStream os = conn.getOutputStream()){
            os.write(json.getBytes("UTF-8"));
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line, out="";
        while((line=br.readLine())!=null) out += line;
        br.close();
        return out;
    }
}
