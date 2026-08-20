# enumの既定ORDINAL保存で外部ステータス照会が一致しないデバッグラボ

この教材は、JPAエンティティのenum保存形式を指定しないため、出荷状態がDB上で整数として保存され、文字列コードを前提とする外部照会契約が壊れる問題を再現・修正します。`@Enumerated`を省略したenumフィールドは、特別な`@EnumeratedValue`がなければ既定で`EnumType.ORDINAL`となります。[1] `ORDINAL`は整数、`STRING`は文字列として保存します。[2]

| 項目 | 内容 |
| --- | --- |
| 対象 | Java 21、Spring Boot 3.4.3、Spring Data JPA、Hibernate、H2、JUnit Jupiter |
| バグコミット | [`215fb8c`](../../commit/215fb8c) — enumを序数で保存する状態を再現する |
| 修正コミット | [`10ce3ba`](../../commit/10ce3ba) — enumを文字列で保存する |
| 直接原因 | `ShipmentStatus`へ`@Enumerated(EnumType.STRING)`を指定していない |
| 実境界 | Spring Data JPAで保存・再読込し、JDBCで物理列を独立に読む統合テスト |

## はじめに

JPAで再読込したenumが正しいからといって、DBの物理値が外部照会や連携の契約に適合しているとは限りません。本ラボでは`ShipmentStatus.SHIPPED`を保存し、JPAの再読込結果と`shipment.status`列の値を分けて検証します。

> 出荷済みを保存したとき、外部照会に使う`status`列は整数`1`ではなく文字列`"SHIPPED"`を保持しなければなりません。

## 最短の開始手順

修正済みの`main`では、次のコマンドで統合テストを実行します。

```bash
mvn --batch-mode clean test
```

`ShipmentRepositoryTest`はJPAの再読込値とJDBCで読む物理列を別々に検証します。`EnumStorageObservationTest`は明示的な`STRING`保存が列挙名を保存することを直接確認します。完全な成功出力は[`evidence/03-fixed-full-test-output.txt`](evidence/03-fixed-full-test-output.txt)に保存しています。

## バグを再現する

未コミット変更のない作業ツリーで、意図した契約差分を確認します。

```bash
git switch --detach 215fb8c
mvn --batch-mode test -Dtest=ShipmentRepositoryTest
git switch main
```

バグ状態でもJPAの再読込は`ShipmentStatus.SHIPPED`になります。しかしJDBCで`status`列を読むと`"1"`であり、文字列コードの契約だけが失敗します。出力は[`evidence/01-bug-service-test-output.txt`](evidence/01-bug-service-test-output.txt)に保存しています。

直接原因を切り出すため、[`EnumStorageObservationTest`](src/test/java/jp/tonbiattack/debuglab/shipment/EnumStorageObservationTest.java)はバグ状態で`SHIPPED.ordinal()`と物理列を比較します。成功出力は[`evidence/02-enum-ordinal-observation-output.txt`](evidence/02-enum-ordinal-observation-output.txt)にあります。

## 最小修正

`Shipment.status`へ保存形式を一つ追加します。

```java
@Enumerated(EnumType.STRING)
private ShipmentStatus status;
```

この変更により、外部照会のための物理列も`"SHIPPED"`になります。既存データの移行、独自AttributeConverter、enumの並び替え、JSON表現、外部HTTP連携は扱いません。調査の全体は[デバッグ記録](docs/debugging-record.md)、既存題材との差分は[新規性レポート](docs/novelty-report.md)を参照してください。

## References

[1]: https://jakarta.ee/specifications/persistence/3.2/apidocs/jakarta.persistence/jakarta/persistence/enumerated "Jakarta Persistence API: Enumerated"
[2]: https://jakarta.ee/specifications/persistence/3.2/apidocs/jakarta.persistence/jakarta/persistence/enumtype "Jakarta Persistence API: EnumType"
