package com.sushmita.github.webclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class WebClient {
    private HttpClient client;
    private List<URI> urls;

    public static void main(String[] args){
        List<URI> uriList = new ArrayList<>();
        List<String> taskList = new ArrayList<>();
        prepareInput(uriList,taskList, args);
        WebClient webClient = new WebClient(uriList);
        webClient.sendTask(taskList);
    }


    private static void prepareInput(List<URI> uris, List<String> tasklist, String[] args){
        for(String arg:args){
            String[] split = arg.split(",");
            uris.add(URI.create(split[0]));
            tasklist.add(split[1]);
        }
    }


    public WebClient(List<URI> urls){
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.urls = urls;
    }

    //send request asynchronously
    public void sendTask(List<String> tasks){
        int count = 0;
        for(URI url:urls){
            HttpRequest request = HttpRequest.newBuilder(url)
                    .setHeader("X-debug", "true")
                    .POST(HttpRequest.BodyPublishers.ofString(tasks.get(0)))
                    .build();
            client.sendAsync(request,HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> { System.out.println(response.statusCode());
                                              return response;
                                            })
                    .thenApply(HttpResponse::body)
                    .thenAccept(System.out::println);;
        }


    }
}
