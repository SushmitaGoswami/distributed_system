package com.sushmita.github.http_server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class WebServer {
    private String STATUS_ENDPOINT = "/status_endpoint";
    private String TASK_ENDPOINT = "/task_endpoint";
    private HttpServer httpServer;

    public static void main(String []args) throws IOException {
        int port = (args.length == 1)?Integer.parseInt(args[0]):8080;
        WebServer webServer = new WebServer(port);
        webServer.startServer();
    }


    public WebServer(int port) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public void startServer(){
        // create context
        HttpContext status_context = httpServer.createContext(STATUS_ENDPOINT);
        HttpContext task_context = httpServer.createContext(TASK_ENDPOINT);

        // set response handler
        status_context.setHandler(this::getStatusHandler);
        task_context.setHandler(this::getTaskHandler);
        httpServer.setExecutor(Executors.newFixedThreadPool(8));
        httpServer.start();

    }

    private void getTaskHandler(HttpExchange httpExchange) throws IOException {
        if(!httpExchange.getRequestMethod().equals("POST")){
            httpExchange.close();
            return;
        }
        Headers requestHeaders = httpExchange.getRequestHeaders();

        long startTime = System.currentTimeMillis();
        String response = calculateResponse(httpExchange.getRequestBody());
        long endTime = System.currentTimeMillis();
        System.out.println(requestHeaders.get("X-debug").get(0));
        if(requestHeaders.containsKey("X-debug") && requestHeaders.get("X-debug").get(0).equalsIgnoreCase("true")){
            String responseHeaderValue = "Operation took " + (endTime-startTime) +" millisecond";
            httpExchange.getResponseHeaders().put("ResponseTime", Arrays.asList(responseHeaderValue));
            System.out.println("response header");
        }

        sendResponse(httpExchange, response);
    }

    private String calculateResponse(InputStream requestBody) throws IOException {
        String result = IOUtils.toString(requestBody, StandardCharsets.UTF_8);
        System.out.println(result);
        long multiplication = 1;
        for(String var : result.split(",")){
            multiplication *= Long.parseLong(var);
        }
        return multiplication + "";
    }

    private void getStatusHandler(HttpExchange httpExchange) throws IOException {
        if(!httpExchange.getRequestMethod().equals("GET")){
            httpExchange.close();
            return;
        }
        String response = "Server is alive";
        sendResponse(httpExchange, response);
    }

    private void sendResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream responseBody = httpExchange.getResponseBody();
        responseBody.write(response.getBytes());
        responseBody.flush();
        responseBody.close();
    }
}
