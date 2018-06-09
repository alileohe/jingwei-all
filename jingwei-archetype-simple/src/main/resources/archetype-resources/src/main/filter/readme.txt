不同的MAVEN的profile使用的是filter下不同的properties配置文件
打包时可以增加-P profile名称
如：mvn -U clean package -Dtest -DfailIfNoTests=false -P build -e 
模板中默认的profile为

不指定-P 默认使用 dev.properties
-P daily 使用的是 daily.properties
-P product 使用的是 product.properties

可以根据自己的需求修改POM中的profile的名称