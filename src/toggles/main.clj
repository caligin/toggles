(ns toggles.main
  (:gen-class)
  (:require [ring.adapter.jetty :as ring-j]
            [ring.middleware.file :as ring-file]
            [toggles.core :refer :all])
  (:import [org.eclipse.jetty.server.handler StatisticsHandler]))

;; wiring

(def storage (make-in-memory-toggle-storage))

(def fetch-toggles (make-fetch-toggles-handler storage))

(def fetch-toggles-token (make-fetch-toggles-token-handler storage))

(def store-toggles (make-store-toggles-handler storage))

(def store-toggles-token (make-store-toggles-token-handler storage))

; TODO -> to a make-dispatch that takes pairs of [condition handler]
(defn dispatch [request]
  (let [uri (:uri request)
        method (:request-method request)]
    (cond
      (and (re-matches #"^/$" uri) (= :get method)) (fetch-toggles request)
      (and (re-matches #"^/$" uri) (= :put method)) (store-toggles request)
      (and (re-matches #"^/[^/]+/?$" uri) (= :get method)) (fetch-toggles-token request)
      (and (re-matches #"^/[^/]+/?$" uri) (= :put method)) (store-toggles-token request)
      :default (notfound request))))

(defn configure-graceful-shutdown [server]
  (let [statistics-handler (StatisticsHandler.)
        default-handler (.getHandler server)]
    (.setHandler statistics-handler default-handler)
    (.setHandler server statistics-handler)
    (.setStopTimeout  server (* 60 1000))
    (.setStopAtShutdown server true)))


(defn -main[]
  (ring-j/run-jetty dispatch {:port 3000  :configurator configure-graceful-shutdown}))

; GET /toggles/ -> map of global toggles
; PUT /toggles/ -> sets map of global toggles (should etag to avoid conflict?)
; GET /toggles/<token> -> map of global toggles if token is not cached, cached map otherwise. Sets caching headers.
; PUT /toggles/<token> -> sets map of toggles on token. (should etag to avoid conflict?)