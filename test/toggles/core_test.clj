(ns toggles.core-test
  (:require [clojure.test :refer :all]
            [toggles.core :refer :all]
            [monger.core :as mg]
            [monger.collection :as mc]))

;TODO how do I make these tests reusable vs multiple impls of proto?
;TODO fetch that cannot fetch/store nil
;TODO what about data validation?
(defn make-toggle-storage-protocol-tests-for [new-storage]
  (testing "fetch from new storage yields the empty map"
    (is (=
      {}
      (fetch (new-storage)))))
  (testing "fetch with token from new storage yields the empty map"
    (is (=
      {}
      (fetch (new-storage) "token"))))
  (testing "fetch yields what has been previously stored"
    (is (=
      {:a-toggle true}
      (fetch
        (store (new-storage) {:a-toggle true})))))
  (testing "fetch on a token yields what has been previously stored on that token"
    (is (=
      {:a-toggle true}
      (fetch
        (store (new-storage) "token" {:a-toggle true})
        "token"))))
  (testing "fetch on a token never stored yields what has been stored on the global"
    (is (=
      {:a-toggle true}
      (fetch
        (store (new-storage) {:a-toggle true})
        "token"))))
  (testing "fetch from a token never stored yields the same result even after the global is stored"
    (let [storage (new-storage)
          from-global (fetch storage "token")]
      (store storage {:a-toggle true})
      (is (=
        from-global
        (fetch storage "token")))))
  (testing "store yields the storage"
    (let [storage (new-storage)]
      (is (=
        storage
        (store storage {:a-toggle true})))))
  (testing "store on token yields the storage"
    (let [storage (new-storage)]
      (is (=
        storage
        (store storage "token" {:a-toggle true}))))))


(deftest toggle-storage-protocol-inmemory-conformance
  (make-toggle-storage-protocol-tests-for make-in-memory-toggle-storage))


; TODO this requires a `docker run --rm -p 27017:27017 mongo` now, find a way to remove the dependency OR automate the spwan of the container
; TODO eww not closing the connections properly. read about fixtures here https://clojure.github.io/clojure/clojure.test-api.html
(defn make-clean-mongo-storage []
  (let [
    conn (mg/connect)]
    (mg/drop-db conn "toggles-test")
    (make-mongo-toggle-storage (mg/get-db conn "toggles-test"))))

(deftest toggle-storage-protocol-mongo-conformance
  (make-toggle-storage-protocol-tests-for make-clean-mongo-storage))