# デバッグ記録: enumの既定ORDINAL保存で外部ステータス照会が一致しない

## 再現手順

バグ状態はコミット`215fb8c`で再現します。

```bash
git switch --detach 215fb8c
mvn --batch-mode test -Dtest=ShipmentRepositoryTest
git switch main
```

修正済み状態は`main`で次を実行します。

```bash
mvn --batch-mode clean test
```

## 観測した事実

`ShipmentStatus.SHIPPED`を保存した後、JPAで再読込した値は`SHIPPED`でした。しかしJDBCで同じ`shipment.status`列を読むと`"1"`でした。したがって、エンティティの保存失敗ではなく、物理列の保存形式だけが外部照会契約と異なります。

| 観測点 | 期待 | バグ状態 |
| --- | --- | --- |
| JPAの再読込 | `SHIPPED` | `SHIPPED` |
| JDBCで読む`status`列 | `"SHIPPED"` | `"1"` |
| 契約テスト | 成功 | 物理列のアサーションだけが失敗 |

失敗出力は[`evidence/01-bug-service-test-output.txt`](../evidence/01-bug-service-test-output.txt)に保存しています。

## 競合仮説

| 仮説 | 検証 | 判断 |
| --- | --- | --- |
| enumが保存されていない | 保存後に別トランザクションでJPA再読込する | `SHIPPED`を返すため棄却 |
| H2固有の値変換が原因 | 物理列をJDBCで読み、`SHIPPED.ordinal()`と比較する | 整数値が一致するため、既定マッピング規則を支持 |
| enum保存形式の指定漏れ | `@Enumerated(EnumType.STRING)`だけを追加して再実行する | 物理列が`"SHIPPED"`となり採用 |

## 原因

`@Enumerated`を明示しないenumフィールドは、`@EnumeratedValue`を持たない場合に`EnumType.ORDINAL`として扱われます。[1] `ORDINAL`はenumを整数として、`STRING`は文字列として保存します。[2] 本ラボでは`Shipment.status`に保存形式の指定がなかったため、`SHIPPED`は列挙順序の整数`1`として物理列へ保存されました。

> `EnumType.ORDINAL`は列挙値を整数として、`EnumType.STRING`は文字列として永続化します。— Jakarta Persistence API [2]

## 最小修正

修正コミット`10ce3ba`では、対象フィールドに`@Enumerated(EnumType.STRING)`を追加しました。この変更だけで、エンティティ再読込と外部照会用の物理列の両方が同じ状態名を表現します。既存の整数データを移行する手順や、enumの名前変更に対する互換性は本ラボの対象外です。

## 回帰保証

### 再発防止テスト

`ShipmentRepositoryTest#persistsStatusNameForExternalStatusLookup`は、JPAでの再読込とJDBCの物理列値を別々に確認します。ORM経由の読み書きだけが成功する状態へ戻った場合でも、外部照会契約の破れを検出します。

`EnumStorageObservationTest#explicitStringMappingStoresEnumName`は、明示的な`STRING`保存が`"SHIPPED"`を保存することを直接確認します。修正後の全テスト出力は[`evidence/03-fixed-full-test-output.txt`](../evidence/03-fixed-full-test-output.txt)にあります。

## スコープ

このラボは新規に保存する一つのenumフィールドの形式だけを扱います。既存DB行の段階的な移行、独自コードを持つenum、AttributeConverter、DBネイティブenum、外部APIのスキーマ契約には適用範囲を広げません。

## References

[1]: https://jakarta.ee/specifications/persistence/3.2/apidocs/jakarta.persistence/jakarta/persistence/enumerated "Jakarta Persistence API: Enumerated"
[2]: https://jakarta.ee/specifications/persistence/3.2/apidocs/jakarta.persistence/jakarta/persistence/enumtype "Jakarta Persistence API: EnumType"
