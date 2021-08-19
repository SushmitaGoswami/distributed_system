package com.sushmita.github.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Producer {
    private String topic = "events";
    private KafkaProducer<Long, String> kafkaProducer;


    public static void main(String[] args){
        Producer producer = new Producer("localhost:9092");
        try {
            producer.sendMessage();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public Producer(String broker_servers){
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker_servers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka");

        kafkaProducer = new KafkaProducer<Long, String>(properties);
    }

    public void sendMessage() throws ExecutionException, InterruptedException {
        int partition = 1;
        for(int i=0;i<10;i++){
            long key = i;
            String value = "test " + i;
            ProducerRecord<Long, String> producerRecord = new ProducerRecord<Long, String>(topic, partition, key, value);
            RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();

            System.out.println(recordMetadata);
        }
    }
}
