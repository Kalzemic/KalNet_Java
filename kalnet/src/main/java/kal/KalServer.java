package kal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
@SuppressWarnings({ "unused", "rawtypes", "unchecked", "UnnecessaryReturnStatement"})
public class KalServer {
    
    private static final HashMap<String,String> typemap=new HashMap()
    {{
        put("html","text/html");
        put("css","text/css");
        put("js","application/javascript");
        put("xml","application/xml");
        put("jpg","image/jpeg");
        put("png","image/png");
    }};
    public static void main(String[] args) throws IOException {
        int port=8090;
        try(ServerSocket kalserv=new ServerSocket(port))
        {
            System.out.println("KalServer Initialized on port "+port);
            while (true) 
            { 
                Socket client= kalserv.accept();
                HandleClient(client);
            }
        }
        catch(IOException e)
        {
            System.out.println("Error "+e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void HandleClient(Socket client) throws IOException
    {
        BufferedReader reader=new BufferedReader(new InputStreamReader(client.getInputStream()));
        String ClientMessage=reader.readLine();
        if(ClientMessage==null ||!ClientMessage.startsWith("GET"))
        {
            return;
        }
        String[] parts = ClientMessage.split(" ");
        String filePath = parts[1].equals("/") ? "kalnet/src/main/webapp/index.html" : parts[1].substring(1);
        File f=new File(filePath);
        if(f.exists() && f.isFile())
        {
            String extension= getFileExtension(filePath);
            String contentType= typemap.getOrDefault(extension, "application/octet-stream");
            String header = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + contentType + "; charset=UTF-8\r\n" +
                                "Content-Length: " + f.length() + "\r\n" +
                                "\r\n";
                // Send the header
                client.getOutputStream().write(header.getBytes());
                try (FileInputStream fileInput = new FileInputStream(f)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInput.read(buffer)) != -1) {
                        client.getOutputStream().write(buffer, 0, bytesRead);
                    }
                }

                client.getOutputStream().flush();
        }
        
        else {
            // Send 404 response if file is not found
            String notFoundResponse = """
                HTTP/1.1 404 Not Found
                Content-Type: text/html; charset=UTF-8
                Content-Length: 58

                <html><body><h1>404 - File Not Found</h1></body></html>
                """;
            client.getOutputStream().write(notFoundResponse.getBytes());
            client.getOutputStream().flush();
        }
    } 
    private static String getFileExtension(String filePath)
    {
        if(filePath.contains("."))
        {
            int lastdot=filePath.lastIndexOf('.');
            return filePath.substring(lastdot+1).toLowerCase();
        }
        return "";

    }
}
