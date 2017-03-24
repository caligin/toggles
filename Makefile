#assume leiningen and docker are present TODO check if they are and fail if not

FPM=docker run -ti --rm -v $(shell pwd)/target:/target caligin/fpm@sha256:3ad357d034f172c6251663c4fd7374ad466afb844c47533a4b0fafe470876c03
ARTIFACT=target/toggles-0.1.0-SNAPSHOT-standalone.jar
SOURCES=$(shell find src/ -type f -name '*.clj')

.PHONY: all clean rpm run test trust-local-cert untrust-local-cert uberjar

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

keystore.jks:
	keytool -genkey -keyalg RSA -alias localhost -keystore keystore.jks -storepass "correct horse battery staple" -keypass "correct horse battery staple" -dname 'CN=localhost' -validity 360 -keysize 2048

toggles-localhost.crt: keystore.jks
	keytool -exportcert -alias localhost -keystore keystore.jks -storepass "correct horse battery staple" | openssl x509 -inform der -outform pem -out $@

trust-local-cert: toggles-localhost.crt
	sudo cp $< /usr/local/share/ca-certificates/$<
	sudo update-ca-certificates
	certutil -d sql:$$HOME/.pki/nssdb -A -t P,, -n $< -i $<

untrust-local-cert:
	sudo rm /usr/local/share/ca-certificates/toggles-localhost.crt
	sudo update-ca-certificates
	certutil -d sql:$$HOME/.pki/nssdb -D -n toggles-localhost.crt

run: $(ARTIFACT) keystore.jks
	java -jar $(ARTIFACT)
