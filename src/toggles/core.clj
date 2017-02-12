(ns toggles.core
  (:require [cheshire.core :as json]
            [monger.collection :as mc]))

(defprotocol ToggleStorage
  (fetch [this] [this token] "fetches all toggles as a map. optionally fetches the toggles associated with a token. token cannot be nil. fetching a token never stored yieds the global, then tht same result until it's stored")
  (store [this toggles] [this token toggles] "stores all toggles as a map. optionally stores a toggle map. token cannot be nil"))

; TODO all the validation.... toggles are a kv map of {:<keyword> <bool>} (for now) not nested, no nils, blah
; TODO actually what if we make it a map of {:<keyword> {:enabled <bool>}} to enable future extensions on the conf of a single toggle?
; TODO now I'm thinking that the two structures for global and per-session might diverge, with the glabal containing {:<toggle> {<conf>}}
; while the per-session is the "evaluated" of that... so we can have a toggle setting that says "A/B test on 10% of users" and when new session
; it's evaluated to "actually active or not for this user"
; TODO not sure that the design with store returning the sotrage itself is any better than returning the inserted toggles... revisit this decision

(defn make-in-memory-toggle-storage []
  (let [
    storage (atom {})]
    (reify ToggleStorage

      (fetch [this]
        (get @storage nil {}))

      (fetch [this token]
        (cond
          (nil? (get @storage token)) (fetch
            (store this token (get @storage nil {}))
            token)
          :else (get @storage token {})))

      (store [this toggles]
        (swap! storage assoc nil toggles) this)

      (store [this token toggles]
        (swap! storage assoc token toggles) this))))

(defn make-mongo-toggle-storage [db]
  (let [
    globals "globals"
    sessions "sessions"]
    (reify ToggleStorage

      (fetch [this]
        (or
          (:toggles (mc/find-map-by-id db globals globals))
          {}))

      (fetch [this token]
        (or
          (:toggles (mc/find-map-by-id db sessions token))
          (fetch
            (store this token (fetch this))
            token)))

      (store [this toggles]
        (mc/insert db globals {:_id globals :toggles toggles})
        this)

      (store [this token toggles]
        (mc/insert db sessions {:_id token :toggles toggles})
        this))))


;; handler ctors and some http/json lingo

(defn ok-json-response "gets a clj datastructure, converts it to json" [clojure-body]
  { :status 200
    :headers {"Content-Type" "application/json"}
    :body (json/generate-string clojure-body)})

(defn nocontent-response []
  { :status 204})

(defn notfound [request]
  { :status 404
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
