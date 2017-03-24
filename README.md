# toggles

[![Build Status](https://travis-ci.org/caligin/toggles.svg?branch=master)](https://travis-ci.org/caligin/toggles)

## Building

### RPM

`make`

Requires leiningen and docker

### Uberjar only

`make uberjar`

Requires leiningen only

## Testing

`make test`

## Running

`make run`

Running directly from the jar or from `lein run` can optionally take a path to a `.clj` config file.

Please note that Toggles requires https and only running via `make run` will generate a local keystore if you don't have one already.

## Trusting the local self-signed certificate

If you want to trust the locally-generated self-signed cert run

`make trust-local-cert`

The certificate is generated on your machine so as long as you don't distribute it around it's ok-ish and you can avoid tricks to avoid browser "red" pages or using the `-k` option in curl, that is more prod-like.

Similarly, run `make untrust-local-cert` to remove it.

## Configuring

Configuration is a `.clj` file containing a map.

Available keys:

### :storage

#### :type :memory

```
:storage {
  :type :memory
  }
```

No other configuration available. Transient in-memory storage, will reset at shutdown and is not clusterable.

#### :type :mongo

```
:storage {
  :type :mongo
  :nodes []
  :options {}
  :credentials []
  :db ""
  }
```

- nodes: addressess in the cluster: a list of pairs `["host" port]`
- options: a map of mongo options, see [monger docs](http://reference.clojuremongodb.info/monger.core.html#var-mongo-options-builder) for the available keys.
- credentials: a list of credentials: a list of triples `["user" "db" "pwd"]`
- db the database name to use, as a string

No other configuration available (coming soon...). MongoDB-backed storage, will try to connect to `localhost:27017`.

### :server

Plase note that httpplain is not an option.

#### :port

Port number for the https server, defaults to `3443`.

#### :keystore

Path to the keystore containing certificate and private key. Defaults to "keystore.jks".

Running locally via `make run` will generate a keystore with a self-signed certificate.

#### :key-password

Password for the keystore. Defaults to "correct horse battery staple".

## API

- `GET` `/toggles/` -> map of global toggles
- `PUT` `/toggles/` -> sets map of global toggles
- `GET` `/toggles/<token>` -> map of global toggles if token is not cached (and caches it), cached map otherwise
- `PUT` `/toggles/<token>` -> sets map of toggles on token