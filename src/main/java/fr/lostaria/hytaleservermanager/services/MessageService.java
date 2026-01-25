package fr.lostaria.hytaleservermanager.services;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class MessageService {

    private final Map<String, BlockingQueue<String>> queues = new ConcurrentHashMap<>();

    private BlockingQueue<String> queue(String nodeId) {
        return queues.computeIfAbsent(nodeId, id -> new LinkedBlockingQueue<>());
    }

    public void send(String nodeId, String message) {
        queue(nodeId).offer(message);
    }

    public String waitNext(String nodeId, Duration timeout) {
        try {
            return queue(nodeId).poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

}
