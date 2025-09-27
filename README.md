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

```java
//Для загрузки фото через Spring на Java и получения ссылки для доступа к ним,
// можно реализовать контроллер, который будет обрабатывать POST-запросыдля загрузки 
// и GET-запросы для получения изображений. Вот пример реализации:

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    private static final String UPLOAD_URL = "https://api.timeweb.cloud/api/v1/storages/buckets/{bucket_id}/object-manager/upload";
    private static final String AUTH_TOKEN = "Bearer " + System.getenv("TIMEWEB_CLOUD_TOKEN");

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", AUTH_TOKEN);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("files", file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(UPLOAD_URL, requestEntity, String.class);

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        }
    }
}

//Получение фото
//Для получения фото по ссылке, предположим, что у вас есть URL-адреса изображений, сохраненные в
// базе данных или другом хранилище. Контроллер может возвращать эти URL-адреса:

@RestController
@RequestMapping("/photos")
public class PhotoController {

    // Предположим, что у вас есть метод для получения URL изображения из базы данных
    @GetMapping("/{photoId}")
    public ResponseEntity<String> getPhotoUrl(@PathVariable String photoId) {
        // Здесь вы должны реализовать логику для получения URL изображения по его ID
        String photoUrl = "https://s3.twcstorage.ru/bucket_name/" + photoId;
        return ResponseEntity.ok(photoUrl);
    }
}
```