#assume leiningen and docker are present TODO check if they are and fail if not

PROJECT:=toggles
FPM=docker run -ti --rm -v $(shell pwd)/target:/target caligin/fpm@sha256:3ad357d034f172c6251663c4fd7374ad466afb844c47533a4b0fafe470876c03
ARTIFACT=target/toggles-0.1.0-SNAPSHOT-standalone.jar
SOURCES=$(shell find src/ -type f -name '*.clj')

include localcert.mk

.PHONY: all clean rpm run test uberjar

all: test rpm

clean:
	lein clean

test:
	lein test

$(ARTIFACT): $(SOURCES)
	lein uberjar

uberjar: $(ARTIFACT)

target/toggles-0.1.0-1.x86_64.rpm: $(ARTIFACT)
	$(FPM) -C target -s dir -t rpm -n toggles -v 0.1.0 -p target/toggles-0.1.0-1.x86_64.rpm $(ARTIFACT)=/usr/local/bin/toggles.jar

rpm: target/toggles-0.1.0-1.x86_64.rpm

run: $(ARTIFACT) $(LOCALCERT_KEYSTORE)
	java -jar $(ARTIFACT)
