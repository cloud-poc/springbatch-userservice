# springbatch-userservice
simple spring batch demo for import user information from files using cron job

tips:
1. provided Test data genarator, it can help to generate csv files for testing purpose, you can adjust the total records and page size
2. overall briefing for this demo can be found on https://cloud-poc.github.io/2019/07/21/Spring-batch-01-single-process-demo/index/#more

referred posts:  
https://blog.51cto.com/13501268/2177746  
https://cloud.tencent.com/developer/article/1096337 

issues:  
1.spring.batch.initialize-schema: always, it will auto execute the initial SQL scripts, but if related tables already exists, may get below issues,but can be ignored  
```
2019-07-21 09:29:26 | DEBUG | [main] o.s.j.d.i.ScriptUtils:505 -
				Failed to execute SQL script statement #2 of class path resource [org/springframework/batch/core/schema-mysql.sql]: CREATE TABLE BATCH_JOB_EXECUTION ( JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY , VERSION BIGINT , JOB_INSTANCE_ID BIGINT NOT NULL, CREATE_TIME DATETIME NOT NULL, START_TIME DATETIME DEFAULT NULL , END_TIME DATETIME DEFAULT NULL , STATUS VARCHAR(10) , EXIT_CODE VARCHAR(2500) , EXIT_MESSAGE VARCHAR(2500) , LAST_UPDATED DATETIME, JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL, constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID) references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID) ) ENGINE=InnoDB
java.sql.SQLSyntaxErrorException: Table 'BATCH_JOB_EXECUTION' already exists
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:120)
```
