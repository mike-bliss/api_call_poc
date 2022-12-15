docker-compose up

aws sqs create-queue --queue-name api-calls-queue --endpoint-url=http://localhost:4566

java -jar -Dspring.batch.job.names=apiCallJob build/libs/startec2demo-0.0.1-SNAPSHOT.jar