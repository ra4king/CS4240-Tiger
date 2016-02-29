OUTPUT_BIN = bin/
OUTPUT_JAR = parser.jar
MANIFEST = src/MANIFEST.MF
PRODUCTION_RULES = edu/cs4240/tiger/parser/ProductionRules.txt

all:
	mkdir -p $(OUTPUT_BIN)
	javac -encoding UTF8 -d $(OUTPUT_BIN) -sourcepath src/ src/edu/cs4240/tiger/Tiger.java
	cp src/$(PRODUCTION_RULES) bin/$(PRODUCTION_RULES)
	jar -cmf $(MANIFEST) $(OUTPUT_JAR) -C $(OUTPUT_BIN) .

clean:
	rm -rf $(OUTPUT_BIN)
	rm -rf $(OUTPUT_JAR)
