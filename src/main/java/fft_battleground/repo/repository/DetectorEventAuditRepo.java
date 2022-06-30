package fft_battleground.repo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.repo.model.DetectorEventAudit;

@Repository
public interface DetectorEventAuditRepo extends JpaRepository<DetectorEventAudit, BattleGroundEventType> {

}
