OUTPUT_BIN = bin/
OUTPUT_JAR_MAIN = parser.jar
OUTPUT_JAR_GEN = tiger_gen.jar
MANIFEST_MAIN = src/MANIFEST-MAIN.MF
MANIFEST_GEN = src/MANIFEST-GEN.MF
PRODUCTION_RULES = edu/cs4240/tiger/parser/ProductionRules.txt

all: clean
	@echo Compiling...
	@mkdir -p $(OUTPUT_BIN)
	@javac -encoding UTF8 -d $(OUTPUT_BIN) -sourcepath src/ src/edu/cs4240/tiger/Tiger.java
	@cp src/$(PRODUCTION_RULES) bin/$(PRODUCTION_RULES)
	@echo Creating $(OUTPUT_JAR_MAIN)...
	@jar -cmf $(MANIFEST_MAIN) $(OUTPUT_JAR_MAIN) -C $(OUTPUT_BIN) .
	@echo Done.

gen: clean
	@echo Compiling...
	@mkdir -p $(OUTPUT_BIN)
	@javac -encoding UTF8 -d $(OUTPUT_BIN) -sourcepath src/ src/edu/cs4240/tiger/generator/TigerSourceGenerator.java
	@cp src/$(PRODUCTION_RULES) bin/$(PRODUCTION_RULES)
	@echo Creating $(OUTPUT_JAR_GEN)...
	@jar -cmf $(MANIFEST_GEN) $(OUTPUT_JAR_GEN) -C $(OUTPUT_BIN) .
	@echo Done.

clean:
	@echo Cleaning...
	@rm -rf $(OUTPUT_BIN)
	@rm -rf $(OUTPUT_JAR)
