# 題材企画: enumの既定ORDINAL保存で外部ステータス照会が一致しない

## 対象

| 項目 | 内容 |
| --- | --- |
| 対象技術 | Java 21、Spring Boot 3.4.3、Spring Data JPA、Hibernate、H2、JUnit Jupiter |
| 対象読者 | enumをJPAエンティティへ保存し、DBの値を他の照会・連携で利用する開発者 |
| 難易度プロファイル | 実践・上級。JPAの再読込が成功してもDBの物理値が契約と異なる境界を扱う。 |
| 選定理由 | `@Enumerated`を省略したenumが整数で保存され、DBを文字列コードとして照会する契約だけが壊れる。既存のJPA教材のバルク更新、orphanRemoval、楽観ロック、JPQL null比較、EntityGraphとは別のマッピング規則である。 |

## 学習する契約

> `ShipmentStatus.SHIPPED`を保存したとき、JPAの再読込だけでなく、外部照会に使う`shipment.status`列も文字列`"SHIPPED"`を保持しなければならない。バグ状態ではJPA再読込は成功する一方、物理列が整数`1`になる。

## 直接原因と対象外

`Shipment.status`のenum保存形式を指定していないため、`@Enumerated`の既定である`EnumType.ORDINAL`が使われる。修正は`@Enumerated(EnumType.STRING)`の明示指定だけである。独自コンバータ、enumの並び替え、スキーマ移行、外部HTTP連携、JSON変換、DBベンダー差異は扱わない。

## 再現設計

| 要素 | 決定 |
| --- | --- |
| 公開境界 | `ShipmentRepository#saveAndFlush`と`findById` |
| 初期状態 | H2へ`shipment-001`、`ShipmentStatus.SHIPPED`を保存する。 |
| Redの観測 | 再読込したenumは`SHIPPED`だが、JDBCで読む`status`列は`"SHIPPED"`であるべきところ`"1"`となる。 |
| 直接観測 | 別の固定IDで保存し、JDBCが`SHIPPED.ordinal()`と同じ整数を返すことを確認する。 |
| 最終状態 | JPAの再読込値とJDBCで読む物理列を独立に確認する。 |
| 決定性 | H2インメモリDB、固定ID、固定enum、`TransactionTemplate`を使い、時刻・乱数・外部I/Oを使わない。 |

## 仮説

| 仮説 | 検証 | 結果 |
| --- | --- | --- |
| A: enum値そのものが保存されていない | JPAで再読込した`ShipmentStatus`を確認する | 再読込は`SHIPPED`であり棄却する。 |
| B: H2の列変換だけが原因である | 物理列をJDBCで直接読み、`SHIPPED.ordinal()`と比較する | 既定のenumマッピング規則を支持する。 |
| C: 保存形式の指定漏れが原因である | `@Enumerated(EnumType.STRING)`だけを追加し、同じテストを再実行する | 修正後に文字列列値となれば採用する。 |

## 予定する履歴

| 順序 | コミットの目的 | 期待する状態 |
| --- | --- | --- |
| 1 | enumを序数で保存する状態を再現する | 物理列の文字列契約だけが失敗する。 |
| 2 | enumを文字列で保存する | 同じ統合テストが成功する。 |
