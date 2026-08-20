# 新規性レポート: enumの既定ORDINAL保存で外部ステータス照会が一致しない

## 結論

本ラボは、JPAエンティティのenum保存形式を省略した結果、`ShipmentStatus.SHIPPED`がDBで整数`1`となり、文字列コード`"SHIPPED"`を必要とする外部照会契約が壊れる問題を扱います。原因は`@Enumerated`の既定ORDINAL保存であり、`@Enumerated(EnumType.STRING)`を追加する一点で修正します。[1] [2]

既存のSpring Data JPA題材であるバルク更新とPersistence Context、orphanRemoval、楽観ロック、JPQL null比較、EntityGraphによるLAZY初期化とは、直接原因、実境界、観測契約、最小修正が異なります。

## 監査方法

2026-08-20に`/home/ubuntu/qiita`の公開・非公開Markdownについて、`@Enumerated`、`EnumType.ORDINAL`、`ORDINAL`、`CascadeType.PERSIST`、`mappedBy`、`readOnly = true`を検索しました。`@Enumerated`、`EnumType.ORDINAL`、`ORDINAL`は既存のQiita原稿に見つかりませんでした。ホームディレクトリ直下の先行教材名も`cascade`、`association`、`readonly`、`flush`で検索しましたが、同一題材は見つかりませんでした。

Repository Catalogは`/home/ubuntu/repository-catalog`に存在しなかったため、更新・検証・自動スクリーニングは実行できませんでした。この制約と、Qiita原稿・ローカル教材を用いた代替監査の範囲を明記します。

## 既存JPA題材との四軸比較

| 比較対象 | 直接原因 | 実境界 | 観測契約 | 最小修正 | 本ラボとの差分 |
| --- | --- | --- | --- | --- | --- |
| 本ラボ | enum保存形式の指定漏れによりORDINALを使う | Repository保存・H2物理列のJDBC照会 | 再読込値と物理列がともに`SHIPPED`である | `@Enumerated(EnumType.STRING)` | 基準 |
| バルク更新とPersistence Context | 更新後に管理中エンティティが古い | `@Modifying`更新と再読込 | 更新値が上書きされない | Contextを同期する | 本ラボはSELECT/UPDATEではなく値マッピングを扱う。 |
| orphanRemoval | 親から外した子が削除されない | 親子コレクションの削除 | 子DB行が消える | 削除伝播を指定する | 本ラボは関連削除でなく単一列の保存形式を扱う。 |
| 楽観ロック | 並行更新のversion競合 | 二つの更新トランザクション | 古い更新を拒否する | `@Version`で競合を扱う | 本ラボは単一トランザクション・単一保存を扱う。 |
| JPQL null比較 | nullを`=`で比較する | `@Query`による検索 | 未割当行を返す | `IS NULL`を使う | 本ラボは検索述語でなくenumの物理表現を扱う。 |
| EntityGraphとLAZY初期化 | デタッチ後に未初期化関連を読む | サービス後のDTO変換 | 明細名を読める | 詳細取得へEntityGraphを指定する | 本ラボは関連取得でなく基本列の永続化を扱う。 |

## 採用判断

既存JPA教材と同じH2・Spring Data JPA統合テストを使いますが、DBの最終観測は関連行、更新競合、クエリ結果ではなくenum列の物理値です。修正もフェッチ、削除伝播、ロック、JPQL条件とは異なるマッピング指定です。教育上独立した原因と契約を持つため、重複しない題材として採用します。

## References

[1]: https://jakarta.ee/specifications/persistence/3.2/apidocs/jakarta.persistence/jakarta/persistence/enumerated "Jakarta Persistence API: Enumerated"
[2]: https://jakarta.ee/specifications/persistence/3.2/apidocs/jakarta.persistence/jakarta/persistence/enumtype "Jakarta Persistence API: EnumType"
