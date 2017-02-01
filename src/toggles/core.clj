(ns toggles.core)

(defprotocol ToggleStorage
	(fetch [this] [this token] "fetches all toggles as a map. optionally fetches the toggles associated with a token. token cannot be nil")
	(store [this toggles] [this token toggles] "stores all toggles as a map. optionally stores a toggle map. token cannot be nil"))

; TODO all the validation.... toggles are a kv map not nested, no nils, blah
(defn make-in-memory-toggle-storage []
	(let [storage (atom {nil {}})]
		(reify ToggleStorage
			(fetch [this] (get @storage nil))
		    (fetch [this token] (get @storage token))
		    (store [this toggles] (swap! storage assoc nil toggles))
		    (store [this token toggles] (swap! storage assoc token toggles)))))


; TODO the facade that has the behaviour "cache if token does not exist"

; TODO storage implementations for something persistent and possibly clusterable