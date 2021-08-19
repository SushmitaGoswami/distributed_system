package com.sushmita.github.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class Consumer {
    private String listofTopics = "events";
    private KafkaConsumer<Long, String> kafkaConsumer;


    public static void main(String[] args){
        String broker_servers = "localhost:9092,localhost:9093,localhost:9094";
        String consumerGroup = args.length==1?args[0]:"defaultGroup";

        Consumer consumer = new Consumer(broker_servers, consumerGroup);
        consumer.consumeMessage();
    }
    public Consumer(String broker_servers, String consumer_group){
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker_servers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumer_group);

        System.out.println("Consumer is a part of consumer_group: " + consumer_group);
        kafkaConsumer = new KafkaConsumer<Long, String>(properties);
        kafkaConsumer.subscribe(Collections.singletonList(listofTopics));
    }

    public void consumeMessage(){
        while(true){
            ConsumerRecords<Long, String> poll =
                    kafkaConsumer.poll(Duration.ofSeconds(1));

            if(poll == null){
                continue;
            }else{
                poll.records(listofTopics).forEach(System.out::println);
            }
            kafkaConsumer.commitAsync();
        }
    }

}
