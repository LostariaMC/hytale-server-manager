package fr.lostaria.hytaleservermanager.services;

import fr.lostaria.hytaleservermanager.entities.Server;
import fr.lostaria.hytaleservermanager.repositories.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class HubAutoStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HubAutoStarter.class);

    public static final String HUB_IMAGE = "hytale-hub";
    private static final int MIN_HUBS = 1;

    private final ServerRepository serverRepository;
    private final ServerService serverService;

    public HubAutoStarter(ServerRepository serverRepository, ServerService serverService) {
        this.serverRepository = serverRepository;
        this.serverService = serverService;
    }

    @Scheduled(fixedDelay = 45_000L, initialDelay = 45_000L)
    public void ensureHubs() {
        long hubs = serverRepository.countByImage(HUB_IMAGE);
        if (hubs >= MIN_HUBS) return;

        LOGGER.info("HUB {}/{} hubs, starting a new one", hubs, MIN_HUBS);
        try {
            Server server = serverService.createServer(HUB_IMAGE);
            LOGGER.info("HUB created id={} node={} port={}", server.getId(), server.getNode().getId(), server.getPort());
        } catch (Exception e) {
            LOGGER.warn("HUB start failed: {}", e.getMessage());
        }
    }
}
