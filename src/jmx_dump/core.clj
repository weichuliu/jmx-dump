(ns jmx-dump.core
  (:require [clojure.java.jmx :as jmx])
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
          :reset    "[00m"
        }
        color (color-name color-dict)
        reset (:reset color-dict)]
    (if (not (nil? color))
      (str \u001b color s \u001b reset)
      s)))

(defn dump-mbeans
  "Given a [host port], list all MBeans and all attributes in each MBeans "
  [jmx-host jmx-port]
  (jmx/with-connection {:host jmx-host :port jmx-port}
    (let [mbean-list (sort (map str (jmx/mbean-names "*:*")))
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
    (let [[jmx-host jmx-port] (map #(System/getenv %) ["JMX_HOST" "JMX_PORT"])]
      ; Check if ENV VAR are set. jmx-port are str but it works.
      (if (or (nil? jmx-host) (nil? jmx-port))
        (throw (Exception. "JMX_HOST and JMX_PORT has to be set, JMX_PORT has to be a port number"))
        (println "JMX_HOST is" jmx-host "and JMX_PORT is" jmx-port))
      ; Get all MBeans' name -> mbean-list
      (dump-mbeans jmx-host jmx-port))))
