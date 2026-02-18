package fr.lostaria.hytaleservermanager.repositories;

import fr.lostaria.hytaleservermanager.entities.HytaleAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HytaleAuthTokenRepository extends JpaRepository<HytaleAuthToken, String> {
}
