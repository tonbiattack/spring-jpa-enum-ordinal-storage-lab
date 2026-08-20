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
    void unspecifiedEnumMappingStoresOrdinalInteger() {
        transactionTemplate.executeWithoutResult(status ->
                repository.saveAndFlush(new Shipment("shipment-observation-001", ShipmentStatus.SHIPPED))
        );

        Integer storedStatus = jdbcTemplate.queryForObject(
                "select status from shipment where shipment_code = ?", Integer.class, "shipment-observation-001"
        );

        assertEquals(ShipmentStatus.SHIPPED.ordinal(), storedStatus,
                "保存形式未指定のenumは列挙順序の整数として保存される");
    }
}
