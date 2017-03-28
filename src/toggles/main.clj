(ns toggles.main
  (:gen-class)
  (:require [ring.adapter.jetty :as ring-j]
            [ring.middleware.file :as ring-file]
            [toggles.core :refer :all]
            [monger.core :as mg]
            [monger.credentials :as mcs])
  (:import [org.eclipse.jetty.server.handler StatisticsHandler]))

;; config

(def default-config {
  :storage {
    :type :memory
  }
  :server {
    :port 3443
    :keystore "toggles-localhost.jks"
    :key-password "correct horse battery staple"
  }})

(defn load-and-default [config-file]
  (merge
    default-config
    (or
      (and
        (not (nil? config-file))
        (.isFile (java.io.File. config-file))
        (read-string (slurp config-file)))
      {})))

(def default-mongo-config {
  :type :mongo
  :nodes [["localhost" 27017]] ; pairs [host port]
  :options {} ; any permitted by monger.core/mongo-options
  :credentials [] ; triples [user db pwd]
  :db "toggles"
  })

(defn connect-mongo [given-mongo-config]
  (let [{:keys [nodes options credentials db]} (merge default-mongo-config given-mongo-config)]
    (mg/get-db
      (mg/connect
        (map (partial apply mg/server-address) nodes)
        (mg/mongo-options options)
        (map (partial apply mcs/create) credentials))
      db)))

(defn make-storage[{:keys [storage] :as config}]
  (cond
    (= :memory (:type storage)) (make-in-memory-toggle-storage)
    ; TODO: heh, what about clean shutdown of the connection? should I pass around an atom where to accumulate things to close?
    (= :mongo (:type storage)) (make-mongo-toggle-storage (connect-mongo storage))
    :default (throw (IllegalArgumentException. (str "unknown storage type: " (:type storage))))))


; routing stuff


; routes -> '(route)
; route -> [url-matcher method handler]
; url-matcher -> regex
; method -> keyword
; hndler -> (fn [request])
(defn make-router [routes]
  (fn [{:keys [uri request-method] :as request}]
    ((nth
      (first
        (filter
          (fn [[url-matcher method _h]]
            (and (re-matches url-matcher uri) (= method request-method)))
        routes))
      2)
      request)))


(defn configure-graceful-shutdown [server]
  (let [statistics-handler (StatisticsHandler.)
        default-handler (.getHandler server)]
    (.setHandler statistics-handler default-handler)
    (.setHandler server statistics-handler)
    (.setStopTimeout server (* 60 1000))
    (.setStopAtShutdown server true)))


(defn -main
  ([] (-main nil))
  ([config-file]
    (let [config (load-and-default config-file)

          storage (make-storage config)

          fetch-toggles (make-fetch-toggles-handler storage)
          fetch-toggles-token (make-fetch-toggles-token-handler storage)
          store-toggles (make-store-toggles-handler storage)
          store-toggles-token (make-store-toggles-token-handler storage)

          routes [[#"^/$" :get fetch-toggles]
                  [#"^/$" :put store-toggles]
                  [#"^/[^/]+/?$" :get fetch-toggles-token]
                  [#"^/[^/]+/?$" :put store-toggles-token]]]

      (ring-j/run-jetty (make-router routes) {
        :ssl-port (get-in config [:server :port])
        :configurator configure-graceful-shutdown
        :http? false
        :ssl? true
        :keystore (get-in config [:server :keystore])
        :key-password (get-in config [:server :key-password])}))))

; GET /toggles/ -> map of global toggles
; PUT /toggles/ -> sets map of global toggles (should etag to avoid conflict?)
; GET /toggles/<token> -> map of global toggles if token is not cached, cached map otherwise. Sets caching headers.
; PUT /toggles/<token> -> sets map of toggles on token. (should etag to avoid conflict?)
