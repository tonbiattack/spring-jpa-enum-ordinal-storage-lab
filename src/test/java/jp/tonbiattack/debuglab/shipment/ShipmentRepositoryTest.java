package jp.tonbiattack.debuglab.shipment;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class ShipmentRepositoryTest {

    @Autowired
    private ShipmentRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persistsStatusNameForExternalStatusLookup() {
        Long shipmentId = transactionTemplate.execute(status ->
                repository.saveAndFlush(new Shipment("shipment-001", ShipmentStatus.SHIPPED)).getId()
        );

        ShipmentStatus reloadedStatus = transactionTemplate.execute(status -> {
            entityManager.clear();
            return repository.findById(shipmentId).orElseThrow().getStatus();
        });
        String storedStatus = jdbcTemplate.queryForObject(
                "select status from shipment where shipment_code = ?", String.class, "shipment-001"
        );

        assertAll(
                () -> assertEquals(ShipmentStatus.SHIPPED, reloadedStatus,
                        "JPAとしては出荷済みの列挙値を再読込できる"),
                () -> assertEquals("SHIPPED", storedStatus,
                        "外部照会に使うstatus列は列挙名を文字列で保持する")
        );
    }
}
