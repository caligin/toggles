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

`lein run`

Can optionally take a path to a `.clj` config file

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
  }
```

No other configuration available (coming soon...). MongoDB-backed storage, will try to connect to `localhost:27017`.

### :server

#### :port

Port number for the http server, defaults to `3000`.

## API

- `GET` `/toggles/` -> map of global toggles
- `PUT` `/toggles/` -> sets map of global toggles
- `GET` `/toggles/<token>` -> map of global toggles if token is not cached (and caches it), cached map otherwise
- `PUT` `/toggles/<token>` -> sets map of toggles on token