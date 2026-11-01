FRONTEND := frontend
COMPOSE := docker compose
BACKEND_CONFIG := backend/src/main/resources/application.yml

.DEFAULT_GOAL := help

.PHONY: help install dev build lint preview clean down backend-config backend-up-dev backend-up-infra backend-test backend-build backend-logs

help: ## Lista las tareas disponibles
	@echo "E3 · Equivalencias USAL"
	@echo ""
	@echo "  make install   Instala las dependencias del frontend (npm ci, respeta el lockfile)"
	@echo "  make dev       Levanta el servidor de desarrollo en http://localhost:5173"
	@echo "  make build     Compila TypeScript y genera el build de producción"
	@echo "  make lint      Corre oxlint sobre el frontend"
	@echo "  make preview   Sirve localmente el build de producción"
	@echo "  make clean     Borra el directorio de build"
	@echo "  make down      Detiene el stack de contenedores"
	@echo ""
	@echo "  make backend-up-dev    Levanta backend, PostgreSQL y pgAdmin"
	@echo "  make backend-up-infra  Levanta solo PostgreSQL y pgAdmin"
	@echo "  make backend-test      Corre los tests del backend dentro de Docker"
	@echo "  make backend-build     Construye la imagen del backend"
	@echo "  make backend-logs      Sigue los logs del backend"

install: ## Instala las dependencias del frontend
	cd $(FRONTEND) && npm ci

dev: ## Levanta el servidor de desarrollo
	cd $(FRONTEND) && npm run dev

build: ## Compila TypeScript y genera el build de producción
	cd $(FRONTEND) && npm run build

lint: ## Corre oxlint sobre el frontend
	cd $(FRONTEND) && npm run lint

preview: ## Sirve localmente el build de producción
	cd $(FRONTEND) && npm run preview

clean: ## Borra el directorio de build
	rm -rf $(FRONTEND)/dist

down: ## Detiene el stack de contenedores
	@if [ -f docker-compose.yml ]; then \
		$(COMPOSE) down; \
	else \
		echo "No hay docker-compose.yml: el stack de contenedores todavía no está definido."; \
		echo "El único servicio en ejecución es el dev server del frontend, que se detiene con Ctrl+C."; \
	fi

backend-config: ## Crea application.yml a partir del ejemplo si todavía no existe
	@test -f $(BACKEND_CONFIG) || cp $(BACKEND_CONFIG).example $(BACKEND_CONFIG)

backend-up-dev: backend-config ## Levanta backend, PostgreSQL y pgAdmin
	$(COMPOSE) up -d --build

backend-up-infra: ## Levanta solo PostgreSQL y pgAdmin
	$(COMPOSE) up -d postgres pgadmin

backend-test: backend-config ## Corre los tests del backend dentro de Docker
	docker build --target test -t e3-backend-test backend

backend-build: backend-config ## Construye la imagen del backend
	$(COMPOSE) build backend

backend-logs: ## Sigue los logs del backend
	$(COMPOSE) logs -f backend
