package jp.tonbiattack.debuglab.shipment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class EnumStorageObservationTest {

    @Autowired
    private ShipmentRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void explicitStringMappingStoresEnumName() {
        transactionTemplate.executeWithoutResult(status ->
                repository.saveAndFlush(new Shipment("shipment-observation-001", ShipmentStatus.SHIPPED))
        );

        String storedStatus = jdbcTemplate.queryForObject(
                "select status from shipment where shipment_code = ?", String.class, "shipment-observation-001"
        );

        assertEquals("SHIPPED", storedStatus,
                "STRING保存を明示したenumは列挙名を文字列として保存する");
    }
}
