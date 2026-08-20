package jp.tonbiattack.debuglab.shipment;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByShipmentCode(String shipmentCode);
}
