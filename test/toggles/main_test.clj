(ns toggles.main-test
  (:require [clojure.test :refer :all]
            [toggles.main :refer :all]
            [ring.adapter.jetty :as ring-j]
            [clj-http.client :as client]))

(deftest can-start-webserver
  (let [server (ring-j/run-jetty (make-router [[#"/" :get (fn [r] r)]]) {:port 3000 :join? false})]
    (testing "can start server"
      (is (= 200 (:status (client/get "http://localhost:3000")))))
    (.stop server)))
