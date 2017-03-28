# TODO check if certutil/keytool present, fail if not
# TODO self-update target?

PROJECT ?= $(notdir $(CURDIR))
PROJECT := $(strip $(PROJECT))
ALIAS := $(PROJECT)-localhost
LOCALCERT_KEYSTORE ?= $(ALIAS).jks
LOCALCERT_CERT ?= $(ALIAS).crt
LOCALCERT_PASSWORD ?= "correct horse battery staple"

.PHONY: all distclean-localcert gitignore-localcert trust-localcert untrust-localcert

# assuming that either the including makefile will use a default target named all or the default target is defined above include #TODO is there a better way?
all:

$(LOCALCERT_KEYSTORE):
	keytool -genkey -keyalg RSA -alias $(ALIAS) -keystore $@ -storepass $(LOCALCERT_PASSWORD) -keypass $(LOCALCERT_PASSWORD) -dname 'CN=localhost' -validity 360 -keysize 2048

$(LOCALCERT_CERT): $(LOCALCERT_KEYSTORE)
	keytool -exportcert -alias $(ALIAS) -keystore $< -storepass $(LOCALCERT_PASSWORD) | openssl x509 -inform der -outform pem -out $@

trust-localcert: $(LOCALCERT_CERT)
	sudo cp $< /usr/local/share/ca-certificates/$<
	sudo update-ca-certificates
	certutil -d sql:$$HOME/.pki/nssdb -A -t P,, -n $(ALIAS) -i $<

untrust-localcert:
	sudo rm /usr/local/share/ca-certificates/$(LOCALCERT_CERT)
	sudo update-ca-certificates
	certutil -d sql:$$HOME/.pki/nssdb -D -n $(ALIAS)

distclean-localcert:
	rm -f $(LOCALCERT_CERT)
	rm -f $(LOCALCERT_KEYSTORE)

distclean:: distclean-localcert

gitignore-localcert:
	@echo $(LOCALCERT_CERT)
	@echo $(LOCALCERT_KEYSTORE)
