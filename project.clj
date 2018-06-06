(defproject jmx-dump "0.1.1"
  :description "Dumping MBeans info from a JMX server"
  :url ""
  :license {:name "Beerware"
            :url "https://fedoraproject.org/wiki/Licensing/Beerware"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jmx "0.3.4"]]
  :main ^:skip-aot jmx-dump.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
