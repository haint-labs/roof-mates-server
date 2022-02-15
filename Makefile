ENV_FILE := ./.env/.env.local

SRCS := $(shell find src -type f)
ARTIFACT := build/libs/roof-mates-0.0.1-all.jar

DOCKER_CMD := docker-compose --env-file $(ENV_FILE) -f deploy/docker-compose.yml -f deploy/docker-compose.local.yml


# Build

$(ARTIFACT): $(SRCS)
	./gradlew shadowJar

build: $(ARTIFACT)


# Development
dev-db:
	$(DOCKER_CMD) up -d database migration

.PHONY: dev
dev: dev-db
	./gradlew build -t

.PHONY: local
local: build
	$(DOCKER_CMD) up

# Clean up

.PHONY: clean-jar
clean-jar:
	./gradlew clean

.PHONY: clean-docker
clean-docker:
	$(DOCKER_CMD) down

.PHONY: clean-db
clean-db:
	-rm -rf ./.postgres/*

.PHONY: clean
clean: clean-jar clean-docker clean-db
