介绍
===

精卫是一个基于MySQL数据库的数据复制组件，基于最最原始的生产者-消费者模型，引入Pipeline（负责数据传送）、Extractor（生产数据）、Applier（消费数据）的概念，构建一套高易用性的数据复制框架。
目前支持源MYSQL,ORACLE,HBASE的数据复制和分发。

![ ](http://baike.corp.taobao.com/images/7/79/Jingwei-%E7%AE%80%E4%BB%8B.png)

功能
===

* DB到DB复制（支持规则）

* DB数据变化通知（改前数据改后数据）

* DB数据变化一对多订阅(需与METAQ配合)

常用场景
===

1.多维度复制，如交易买家维度和卖家维度复制。

2.数据变化通知，如VSEARCH利用数据变化通知构建异步索引。

3.一次DUMP多次订阅，如商品数据变化通知服务于DETAIL 静态化，P4P商品管理，商品标签平台等。

安全保证
===

+ 任务无状态化，配置ZK管理，任何任务故障都会尝试切换到其他机器上。

+ DUMP的数据库自动切换，结合JADE与DBA管里的数据库切换同步,保证数据的安全性不丢失。

+ 完善的监控报警机制，任务堆积，故障都有监控保证及时通过手机和旺旺通知到负责人。

+ 高效的调度DUMP机制，保证延迟最小（毫秒级别）

[更多内容(猛点我)](http://baike.corp.taobao.com/index.php/TDDL_mysql_replicator)
====

