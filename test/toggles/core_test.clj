(ns toggles.core-test
  (:require [clojure.test :refer :all]
            [toggles.core :refer :all]))

;TODO how do I make these tests reusable vs multiple impls of proto?
;TODO fetch that cannot fetch/store nil
;TODO what about data validation?
(deftest in-memory-toggle-storage-protocol
  (let [make make-in-memory-toggle-storage]
    (testing "fetch from new storage yields the empty map"
      (is (=
        {}
        (fetch (make)))))
    (testing "fetch with token from new storage yields the empty map"
      (is (=
        {}
        (fetch (make) "token"))))
    (testing "fetch yields what has been previously stored"
      (is (=
        {"toggle" true}
        (fetch
          (store (make) {"toggle" true})))))
    (testing "fetch on a token yields what has been previously stored on that token"
      (is (=
        {"toggle" true}
        (fetch
          (store (make) "token" {"toggle" true})
          "token"))))
    (testing "fetch on a token never stored yields what has been stored on the global"
      (is (=
        {"toggle" true}
        (fetch
          (store (make) {"toggle" true})
          "token"))))
    (testing "fetch from a token never stored yields the same result even after the global is stored"
      (let [storage (make)
            from-global (fetch storage "token")]
        (store storage {"toggle" true})
        (is (=
          from-global
          (fetch storage "token")))))
    (testing "store yields the storage"
      (let [storage (make)]
        (is (=
          storage
          (store storage {"toggle" true})))))
    (testing "store on token yields the storage"
      (let [storage (make)]
        (is (=
          storage
          (store storage "token" {"toggle" true})))))))
