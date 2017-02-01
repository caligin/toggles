(ns toggles.main
	(:require [ring.adapter.jetty :as ring-j]
              [ring.middleware.file :as ring-file]
              [cheshire.core :as json]
              [toggles.core :refer :all]))

;; handler ctors and some http/json lingo

(defn ok-json-response "gets a clj datastructure, converts it to json" [clojure-body]
	{:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string clojure-body)})

(defn nocontent-response []
	{:status 204})

(defn notfound [request]
  {:status 404
   :headers {"Content-Type" "application/json"}})


(defn token [request]
	(nth
		(re-matches #"^/([^/]+)/?$" (:uri request))
		1))

(defn make-fetch-toggles-handler [storage]
	(fn [request]
		(ok-json-response (fetch storage))))

(defn make-fetch-toggles-token-handler [storage]
	(fn [request]
		(ok-json-response (fetch storage (token request)))))

(defn make-store-toggles-handler [storage]
	(fn [request]
		(store storage (json/parse-stream (clojure.java.io/reader (:body request))))
		(nocontent-response)))

(defn make-store-toggles-token-handler [storage]
	(fn [request]
		(store storage (token request) (json/parse-stream (clojure.java.io/reader (:body request))))
		(nocontent-response)))


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

(ring-j/run-jetty dispatch {:port 3000})



; GET /toggles/ -> map of global toggles
; PUT /toggles/ -> sets map of global toggles (should etag to avoid conflict?)
; GET /toggles/<token> -> map of global toggles if token is not cached, cached map otherwise. Sets caching headers.
; PUT /toggles/<token> -> sets map of toggles on token. (should etag to avoid conflict?)