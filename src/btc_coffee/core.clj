(ns btc-coffee.core
  (:gen-class)
  (:use 
        (compojure [core :only [defroutes GET POST]]
                   [route :only [files]]
                   [handler :only [site]])
        [clojure.tools.logging :only [info]]        
        ring.middleware.json
        pawnshop.core
        org.httpkit.server
        org.httpkit.timer
        [clojure.tools.cli :only [cli]]
        ring.util.response
        hiccup.core
        hiccup.page)
  (:require 
            [org.httpkit.dbcp :as db]
            [ring.middleware.reload :as reload]
            [clj-http.client :as client]
            [clojure.edn])

  (:import
   (java.io ByteArrayOutputStream
            ByteArrayInputStream)
   (com.google.zxing.*)
   (com.google.zxing.common.*)
   (com.google.zxing.client.j2se.*)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;config



(defn load-config []
  (def config  (read-string (slurp (str (System/getProperty "user.home") java.io.File/separator ".btc-coffee.clj"))))
  )
;;(load-config)

(defn get-config [key]
  (get config key))

(defn curconv "convert between currencies"
  [amount from to ]
  (let [rates (get (get (client/get (format  "http://openexchangerates.org/api/latest.json?app_id=%s"
                                             (get-config :ocx-key)) {:as :json}) :body) :rates)]
    (* (* amount  (get rates to)
          (/ 1  (get rates from))))))

(defn coffee-bitcoin-uri-conf []
  (format "bitcoin:%s?amount=%5f&label=%s&message=%s"
                             (get-config :account)
                             (curconv (get-config :amount) (get-config :from) :BTC )
                             (get-config :label)                             
                             (clj-http.util/url-encode (get-config :message)  )))

(defn qr-encode-to-stream
  "qr encode and stuf"
  [req]
  (let [size 500
        uri (coffee-bitcoin-uri-conf)
        out-stream (ByteArrayOutputStream.)
        in-stream
        (do 
          (com.google.zxing.client.j2se.MatrixToImageWriter/writeToStream
           (.encode (com.google.zxing.MultiFormatWriter.) uri com.google.zxing.BarcodeFormat/QR_CODE size size )
           "png"
           out-stream)
          (ByteArrayInputStream.
           (.toByteArray out-stream)))        
        header {:status 200
                :headers {"Content-Type" "image/png"}}]
    in-stream
    ))


(defn qr-wrapper
  [req]
  (html
   (html5
    [:head 
     (include-css "/chartroom.css")
     (include-js "/jquery-1.7.2.js")
     (include-js "/coffee.js")]
    [:body [:h1#message "Scan!" ]
     [:img {:src "/btc-coffee/qr"}
      ]]))
  )


(def walletnotify-transactions (ref {}))

(def clients (atom {}))                 ; a hub, a map of client => sequence number

(defn send-message [message]
    (doseq [client (keys @clients)]
    (send! client
           message
          false )
    ))

(defn payment-received
  "a payment has arrived"
  []
  ()
  (send-message        (str "payment " (java.util.Date.)))

  ( schedule-task 2000 (send-message "scan again"))
  )


(defn bitcoin [& rest]
  (apply  (bitcoin-proxy (get-config :bitcoind-url) (get-config :bitcoind-user) (get-config :bitcoind-password) ) rest))

(defn walletnotify-callback
  "walletnotify-callback, when bitcoind receives a transaction for our wallet"
  [txid]
  (dosync
   ;;how many times have we seen this transaction?
   (alter walletnotify-transactions update-in [txid] #(if (number? %) (inc %) 1))
   (if (and 
        (= 1 (get @walletnotify-transactions txid))
        (= (get-config :address)) (get (get (get (bitcoin :gettransaction txid) :details) 0) :address))
     ;;"1st trans", correct address
     (payment-received)
     ;;"not 1st trans"
     "nope"
     )
   )
  
  )



(defn msg-handler [req]
  (with-channel req channel
    (info channel "connected") ;;needs logging api
    (swap! clients assoc channel true)
    (on-close channel (fn [status]
                        (swap! clients dissoc channel)
                        (info channel "closed, status" status)
                        ))))
(defn btc-info
  [req]
  (html [:span {:class "foo"}  (format "coffee bitcoin: %s" (bitcoin :getreceivedbyaccount "coffee" 0))])
  )


(defroutes app-routes
  (GET "/btc-coffee/ws" []  msg-handler)
  (GET "/btc-coffee/hello" [] "Hello, Coffee World yay yay!")
  (GET "/btc-coffee/info" [] btc-info)
  (GET "/btc-coffee/qr" [] qr-encode-to-stream)
  (GET "/btc-coffee/qr-wrapper" [] qr-wrapper)
  (GET "/btc-coffee/walletnotify-callback/:txid" [txid]
       (walletnotify-callback txid))
  (files "" {:root "static"})
  (compojure.route/not-found "Coffe Really Not Found"))




(defn -main
  "main."
  [& args]
  (load-config)

  (println "Hello, BTC Coffee World! ")
  (let [handler (if true ;(in-dev? args)
                  (reload/wrap-reload (site #'app-routes)) ;; only reload when dev
                  (site app-routes))]
    (run-server handler {:port (get-config :port)}))

  )
