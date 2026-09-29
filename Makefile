FRONTEND := frontend
COMPOSE := docker compose

.DEFAULT_GOAL := help

.PHONY: help install dev build lint preview clean down

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
	@echo "Solo hay tareas de frontend: el backend todavía no está implementado."

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
