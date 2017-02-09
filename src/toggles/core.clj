(ns toggles.core
	(:require [cheshire.core :as json]))

(defprotocol ToggleStorage
	(fetch [this] [this token] "fetches all toggles as a map. optionally fetches the toggles associated with a token. token cannot be nil. fetching a token never stored yieds the global, then tht same result until it's stored")
	(store [this toggles] [this token toggles] "stores all toggles as a map. optionally stores a toggle map. token cannot be nil"))

; TODO all the validation.... toggles are a kv map not nested, no nils, blah

(defn make-in-memory-toggle-storage []
	(let [storage (atom {})]
		(reify ToggleStorage
			(fetch [this] (get @storage nil {}))
		    (fetch [this token] (cond
		    						(nil? (get @storage token)) (fetch
		    														(store this token (get @storage nil {}))
		    														token)
		    						:else (get @storage token {})))
		    (store [this toggles] (swap! storage assoc nil toggles) this)
		    (store [this token toggles] (swap! storage assoc token toggles) this))))


; TODO storage implementations for something persistent and possibly clusterable


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
