# Directory del progetto
MAKEFILE_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
PROJECT_DIR := $(MAKEFILE_DIR)/gwt-notes-maven

CLIENT_MODULE := notes-client
SERVER_MODULE := notes-server
SHARED_MODULE := notes-shared

.PHONY: help codeserver run-server compile-server test clean-test

help:
	@echo ""
	@echo "Comandi disponibili per il progetto SwEng:"
	@echo ""
	@echo "  make help            - Mostra questo help"
	@echo "  make codeserver      - Avvia il GWT CodeServer per lo sviluppo client"
	@echo "  make run-server      - Avvia Jetty sul modulo server in modalità dev (http://localhost:8080)"
	@echo "  make compile-server  - Ricompila solo il modulo server (dopo modifiche backend)"
	@echo "  make test            - Esegue tutti i test del progetto"
	@echo "  make clean-test      - Pulisce prima di eseguire i test"
	@echo ""

codeserver:
	cd $(PROJECT_DIR) && mvn -U -e gwt:codeserver -pl $(CLIENT_MODULE) -am

run-server:
	cd $(PROJECT_DIR) && mvn -U jetty:run -pl $(SERVER_MODULE) -am -Denv=dev

compile-server:
	cd $(PROJECT_DIR) && mvn -U compile -pl *$(SERVER_MODULE) -am

test:
	cd $(PROJECT_DIR) && mvn -U test


clean-test:
	cd $(PROJECT_DIR) && mvn clean test