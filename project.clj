(defproject btc-coffee "0.1.0-SNAPSHOT"
  :description "A bitcoin operated coffee machine"
  :url "https://github.com/jave/btc-coffee"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.google.zxing/core "2.2"]
                 [com.google.zxing/javase "2.2"]
                 [clj-http "0.7.6"]
                 [com.cemerick/pomegranate "0.2.0"]
                 [pawnshop "0.1.0-SNAPSHOT"]
                 [compojure "1.1.5"]
                 [ring/ring-devel "1.1.8"]
                 [ring/ring-core "1.1.8"]
                 [ring/ring-json "0.2.0"]
                 [org.clojure/tools.cli "0.2.1"]
                 [http-kit/dbcp "0.1.0"]
                 [http-kit "2.0.1"]
                 [clj-http "0.7.6"]
                 [hiccup "1.0.4"]
                 [org.clojure/tools.logging "0.2.3"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]]
  :main btc-coffee.core
  :aot :all
  :profiles {:uberjar {:aot :all}})
