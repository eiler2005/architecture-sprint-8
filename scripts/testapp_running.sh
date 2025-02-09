#!/bin/bash

name: running test

INIT_MODE=false

echo "Init docker mode configuration = ${INIT_MODE} with keycloak"

# Проверяем, включен ли режим INIT_MODE
if [ "${INIT_MODE}" == "true" ]; then
    echo "Waiting for keycloak to start"
    docker-compose up keycloak_db keycloak  # Запускаем Keycloak и его БД
fi

# Создаем Docker-образ API
docker build -t reports-api api/

# Запускаем все контейнеры в фоновом режиме с пересборкой
#docker-compose up -d --build

# Запускаем API в контейнере
docker-compose up api

# Запускаем фронтенд
docker-compose up frontend

echo "Goodbye :)"

# перезапуск контейнеров

# docker-compose down -v
# docker-compose up -d --build

# сборка приложения на Java

# Сборка backend-приложения (API) с пропуском тестов
# /opt/homebrew/Cellar/maven/3.9.9/bin/mvn -f api/pom.xml clean package -DskipTests
# Запускаем тесты
#/opt/homebrew/Cellar/maven/3.9.9/bin/mvn -f api/pom.xml test