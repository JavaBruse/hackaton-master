# Используем легковесный образ с JRE
FROM eclipse-temurin:17-jre-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем .jar файл в контейнер
COPY target/master-1.0.jar /app/master-1.0.jar

# Используем переменную окружения SPRING_PROFILES_ACTIVE для запуска
CMD ["sh", "-c", "java -jar /app/master-1.0.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
