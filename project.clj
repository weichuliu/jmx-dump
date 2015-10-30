(defproject jmx-dump "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/java.jmx "0.3.1"]]
  :main ^:skip-aot jmx-dump.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
