package fr.lostaria.hytaleservermanager.repositories;

import fr.lostaria.hytaleservermanager.entities.HytaleAuthToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface HytaleAuthTokenRepository extends JpaRepository<HytaleAuthToken, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from HytaleAuthToken t where t.id = 'primary'")
    Optional<HytaleAuthToken> findPrimaryForUpdate();
}
