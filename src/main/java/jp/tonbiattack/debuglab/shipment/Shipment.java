package jp.tonbiattack.debuglab.shipment;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shipmentCode;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    protected Shipment() {
    }

    public Shipment(String shipmentCode, ShipmentStatus status) {
        this.shipmentCode = shipmentCode;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getShipmentCode() {
        return shipmentCode;
    }

    public ShipmentStatus getStatus() {
        return status;
    }
}
