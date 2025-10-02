```shell
    task_id UUID REFERENCES tasks(id) ON DELETE SET NULL, --потом исправить ON DELETE CASCADE
```

```shell
#!/bin/bash
set -e

NETWORK_NAME="app_network"
ADMIN_USER="admin"
ADMIN_PASSWORD="2egE3f690JkejwEopij60qjnioqiowe98"

# --- Чистим старые контейнеры ---
for CONTAINER_NAME in "controller-1" "controller-2" "controller-3" "broker-1" "broker-2" "broker-3"; do
  if docker ps -a --filter "name=$CONTAINER_NAME" --format "{{.Names}}" | grep -q "$CONTAINER_NAME"; then
    docker stop $CONTAINER_NAME || true
    docker rm $CONTAINER_NAME || true
  fi
done

# --- Создаем сеть если не существует ---
docker network create --subnet=172.20.0.0/16 $NETWORK_NAME 2>/dev/null || true

# --- Запускаем контроллеры ---
for i in 1 2 3; do
  docker run --name controller-$i \
    --network $NETWORK_NAME --ip 172.20.0.$((19+i)) \
    --restart unless-stopped -d \
    -e KAFKA_NODE_ID=$i \
    -e KAFKA_PROCESS_ROLES=controller \
    -e KAFKA_LISTENERS=CONTROLLER://:9093 \
    -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
    -e KAFKA_CONTROLLER_QUORUM_VOTERS="1@controller-1:9093,2@controller-2:9093,3@controller-3:9093" \
    apache/kafka:3.9.0
done

sleep 15

# --- Запускаем брокеры с PLAINTEXT для создания пользователя ---
for i in 1 2 3; do
  PORT=$((29092 + (i-1)*10000))
  NODE_ID=$((i+3))

  docker run --name broker-$i \
    --network $NETWORK_NAME --ip 172.20.0.$((22+i)) \
    -p $PORT:9094 \
    --restart unless-stopped -d \
    -v /opt/kafka/config/kafka_server_jaas.conf:/opt/kafka/config/kafka_server_jaas.conf \
    -e KAFKA_NODE_ID=$NODE_ID \
    -e KAFKA_PROCESS_ROLES=broker \
    -e KAFKA_LISTENERS="PLAINTEXT://:9092,SASL_PLAINTEXT://:9094" \
    -e KAFKA_ADVERTISED_LISTENERS="PLAINTEXT://broker-$i:9092,SASL_PLAINTEXT://5.129.246.42:$PORT" \
    -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP="PLAINTEXT:PLAINTEXT,SASL_PLAINTEXT:SASL_PLAINTEXT,CONTROLLER:PLAINTEXT" \
    -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
    -e KAFKA_SASL_ENABLED_MECHANISMS=SCRAM-SHA-256 \
    -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
    -e KAFKA_CONTROLLER_QUORUM_VOTERS="1@controller-1:9093,2@controller-2:9093,3@controller-3:9093" \
    -e KAFKA_OPTS="-Djava.security.auth.login.config=/opt/kafka/config/kafka_server_jaas.conf" \
    apache/kafka:3.9.0
done

# --- Ждем запуск брокеров ---
echo "Ждём запуск брокеров..."
sleep 45

# --- Создаем пользователя через PLAINTEXT ---
docker exec broker-1 /opt/kafka/bin/kafka-configs.sh \
  --bootstrap-server broker-1:9092 \
  --alter --add-config "SCRAM-SHA-256=[password=$ADMIN_PASSWORD]" \
  --entity-type users --entity-name $ADMIN_USER

echo "Кластер успешно запущен!"

```

```shell
"C:\Program Files\OffsetExplorer3\offsetexplorer.exe" -J-Djava.security.auth.login.config="C:\client_jaas.conf"
#для запуска offset
```
