#assume leiningen and docker are present TODO check if they are and fail if not

FPM=docker run -ti --rm -v $(shell pwd)/target:/target caligin/fpm@sha256:3ad357d034f172c6251663c4fd7374ad466afb844c47533a4b0fafe470876c03

.PHONY: all clean rpm test uberjar

all: test rpm

clean:
	lein clean

test:
	lein test

target/toggles-0.1.0-SNAPSHOT-standalone.jar:
	lein uberjar

uberjar: target/toggles-0.1.0-SNAPSHOT-standalone.jar

target/toggles-0.1.0-1.x86_64.rpm: target/toggles-0.1.0-SNAPSHOT-standalone.jar
	$(FPM) -C target -s dir -t rpm -n toggles -v 0.1.0 -p target/toggles-0.1.0-1.x86_64.rpm toggles-0.1.0-SNAPSHOT-standalone.jar=/usr/local/bin/toggles.jar

rpm: target/toggles-0.1.0-1.x86_64.rpm
