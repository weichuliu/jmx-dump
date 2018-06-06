(ns jmx-dump.core
  (:require [clojure.java.jmx :as jmx])
  (:import [sun.management.ConnectorAddressLink])
  (:gen-class))

(defn colorize [color-name s]
  "Make s be colored under bash using ASCII escape sequence"
  (let [color-dict {
    :black    "[30m"
    :red      "[31m"
    :green    "[32m"
    :yellow   "[33m"
    :blue     "[34m"
    :magenta  "[35m"
    :cyan     "[36m"
    :lblack   "[90m"
    :lred     "[91m"
    :lgreen   "[92m"
    :lyellow  "[93m"
    :lblue    "[94m"
    :lmagenta "[95m"
    :lcyan    "[96m"
    :reset    "[00m"}
    color (if (System/getenv "JMX_NOCOLOR") nil (color-name color-dict))
    reset (:reset color-dict)]
    (if (not (nil? color))
      (str \u001b color s \u001b reset)
      s)))

(defn gen-credential
  "If both user and password exist, generate a credential hash-map from it, else a empty map"
  [jmx-user jmx-password]
  (if (and jmx-user jmx-password)
    (hash-map javax.management.remote.JMXConnector/CREDENTIALS (into-array String [jmx-user jmx-password]))
    (hash-map)))

(defn dump-mbeans
  "Connect to a jmx rmi with either [jmx-url] or [jmx-host jmx-port jmx-user jmx-password].
  then list MBeans matching [jmx-query], with attributes in each MBeans"
  [jmx-url jmx-host jmx-port jmx-query jmx-user jmx-password]
  (jmx/with-connection {:url jmx-url
                        :host jmx-host :port jmx-port
                        :environment (gen-credential jmx-user jmx-password)}
    (let [mbean-list (sort (map str (jmx/mbean-names jmx-query)))
          red     #(colorize :red %)
          green   #(colorize :green %)
          lblue   #(colorize :lblue %)
          yellow  #(colorize :yellow %)
          lyellow #(colorize :lyellow %)]
      (doseq [mbean mbean-list]
        ; Print out MBeans Name
        (println (green "MBean:") mbean)
        (let [attributes (jmx/attribute-names mbean)]
          ; Print out All Attributes of the MBean
          (println (lblue "Attributes:") attributes)
          (doseq [attr-name (sort attributes)]
            (try
              (let [attr-value (jmx/read mbean attr-name)]
                ; For all attributes, print attr-name/attr-value pairs
                ; If attr-value is a composite data(map), iterate attr-value and print each k/v pairs
                (if (map? attr-value)
                  (do
                    (println " " (yellow attr-name))
                    (doseq [[k v] (sort attr-value)] (println "   " (lyellow k) v)))
                  (println " " (yellow attr-name) attr-value)
                  ))
              (catch Exception e (println " " (red (str attr-name "is not readable")))))))))))

(defn -main
  "mian"
  ([]
    (let [
      jmx-host (System/getenv "JMX_HOST")
      jmx-port (System/getenv "JMX_PORT")
      jmx-query (or (System/getenv "JMX_QUERY") "*:*")
      jmx-user (System/getenv "JMX_USER")
      jmx-password (System/getenv "JMX_PASSWORD")
      jmx-pid (System/getenv "JMX_PID")
      ]
      ; Check if ENV VAR are set. jmx-port are str but it works.
      (if (and (nil? jmx-pid) (some nil? [jmx-host jmx-port]))
        (throw (Exception. "Either JMX_PID or JMX_HOST+JMX_PORT has to be set, JMX_PORT has to be a port number"))
        (let [jmx-url (if (some? jmx-pid) (sun.management.ConnectorAddressLink/importFrom (Integer. jmx-pid)) nil)]
          (if (some? jmx-pid)
            (printf "Querying '%s' to %n" jmx-pid)
            (printf "Querying '%s' to %s:%s (%s:%s)%n" jmx-query jmx-host jmx-port jmx-user jmx-password))
          ; Get all MBeans' name -> mbean-list
          (dump-mbeans jmx-url jmx-host jmx-port jmx-query jmx-user jmx-password))))))
