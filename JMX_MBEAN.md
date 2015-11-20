# JMX Instrumentation Introduction
this is a quick tutorial that describes:

- How JMX instrumentation work (Read only), some knowledge of MBean structures
- How to query and explore JMX with [clojure/java.jmx](https://github.com/clojure/java.jmx) library

## Set up a JMX server and a JMX agent
Here I will start a leiningen REPL which includes `java.jmx` library. By default the REPL itself runs a JMX Server and `java.jmx` are connecting to it. It is a minimum set to demonstrate JMX query. Sounds Pythonic, but it's a clojure!  
![](https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/Ouroboros-simple.svg/100px-Ouroboros-simple.svg.png)  
Suppose you get [leiningen](leiningen.org) already.  
First initialize a new project that brings clojure/java.jmx:

```
$ lein new app learnjmx
```

Then follow the README of [clojure/java.jmx](https://github.com/clojure/java.jmx), add the library to dependency in `project.clj`:

```
(defproject jmxlearn "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/java.jmx "0.3.1"]]
  :main ^:skip-aot jmxlearn.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
```

Start by `lein repl`, import jmx library:
```
jmxlearn.core=> (require '[clojure.java.jmx :as jmx])
nil
jmxlearn.core=>
```

## List MBeans
In JMX, the only thing you'd care about are ™®MBeans®™. MBeans will register themself with *name* to JMX, from JMX console we get a MBean/MBeans by querying.
For more information, here is the [official doc](https://docs.oracle.com/javase/7/docs/api/javax/management/ObjectName.html)
The query of MBean's name supports globbing. First, let us list all MBeans we can get.

```
; Query to get all MBeans
jmxlearn.core=> (jmx/mbean-names "*:*")
#{#object[javax.management.ObjectName 0xe12a8ed "java.lang:type=MemoryPool,name=Metaspace"] #object[javax.management.ObjectName 0x3c8ab086 "java.lang:type=MemoryPool,name=PS Old Gen"] ...... #object[javax.management.ObjectName 0x5e5767ab "com.sun.management:type=HotSpotDiagnostic"]}

; Get the name from each MBeans, instead of MBean itself
jmxlearn.core=> (map println (sort (map str (jmx/mbean-names "*:*"))))
JMImplementation:type=MBeanServerDelegate
com.sun.management:type=DiagnosticCommand
com.sun.management:type=HotSpotDiagnostic
java.lang:type=ClassLoading
java.lang:type=Compilation
java.lang:type=GarbageCollector,name=PS MarkSweep
java.lang:type=GarbageCollector,name=PS Scavenge
java.lang:type=Memory
java.lang:type=MemoryManager,name=CodeCacheManager
java.lang:type=MemoryManager,name=Metaspace Manager
java.lang:type=MemoryPool,name=Code Cache
java.lang:type=MemoryPool,name=Compressed Class Space
java.lang:type=MemoryPool,name=Metaspace
java.lang:type=MemoryPool,name=PS Eden Space
java.lang:type=MemoryPool,name=PS Old Gen
java.lang:type=MemoryPool,name=PS Survivor Space
java.lang:type=OperatingSystem
java.lang:type=Runtime
java.lang:type=Threading
java.nio:type=BufferPool,name=direct
java.nio:type=BufferPool,name=mapped
java.util.logging:type=Logging

; Globbing. Although Memory/MemoryManager/MemoryPool doesnot related to each other, they are returned together
jmxlearn.core=> (map println (sort (map str (jmx/mbean-names "java.lang:type=Memo*,*"))))
java.lang:type=Memory
java.lang:type=MemoryManager,name=CodeCacheManager
java.lang:type=MemoryManager,name=Metaspace Manager
java.lang:type=MemoryPool,name=Code Cache
java.lang:type=MemoryPool,name=Compressed Class Space
java.lang:type=MemoryPool,name=Metaspace
java.lang:type=MemoryPool,name=PS Eden Space
java.lang:type=MemoryPool,name=PS Old Gen
java.lang:type=MemoryPool,name=PS Survivor Space
```

## MBean Naming
The Name of MBean contains 2 parts: *Domain* and a *list of tag-value pairs*, which are splitted by ":"  
Take "java.lang:type=MemoryPool,name=Metaspace" as an example:

```
; Get a single MBean Object
; The doc of MBean Object https://docs.oracle.com/javase/7/docs/api/javax/management/ObjectName.html
jmxlearn.core=> (def a-mbean (first (jmx/mbean-names "java.lang:type=MemoryPool,name=*")))
#'jmxlearn.core/a-mbean

jmxlearn.core=> a-mbean
#object[javax.management.ObjectName 0x4e33d2f7 "java.lang:type=MemoryPool,name=Metaspace"]

; The full name of a-mbean
jmxlearn.core=> (str a-mbean)
"java.lang:type=MemoryPool,name=Metaspace"

; Domain
jmxlearn.core=> (.getDomain a-mbean)
"java.lang"

; KeyPropertyList sorted by keys' alphabetical order 
jmxlearn.core=> (.getCanonicalKeyPropertyListString (first (jmx/mbean-names "java.lang:type=MemoryPool,name=Code Cache")))
"name=Code Cache,type=MemoryPool"
; Get property by key
jmxlearn.core=> (.getKeyProperty (first (jmx/mbean-names "java.lang:name=Metaspace,type=MemoryPool")) "name")
"Metaspace"
; Keys have no order
jmxlearn.core=> (= (first (jmx/mbean-names "java.lang:name=Metaspace,type=MemoryPool"))
           #_=>    (first (jmx/mbean-names "java.lang:type=MemoryPool,name=Metaspace")))
true
```

Note that For most MBeans, keys are "type" and "name", but it's not mandatory, keys can be any string.  
Also note that Key/Property can be empty, such as "java.lang:type=Memory". This is important when configuring collectd/GenericJMX

## MBean Attributes
After we obtain a mbean, the real data we are interested in are the `Attribute` it contains.

```
; Just send the name string to the function then you get all Attribute as keys.
; Viva Clojure!! Live long and Prosper Clojure! 🖖!
jmxlearn.core=> (jmx/attribute-names "java.lang:name=Metaspace,type=MemoryPool")
(:Name :Type :Valid :Usage :PeakUsage :MemoryManagerNames :UsageThreshold :UsageThresholdExceeded :UsageThresholdCount :UsageThresholdSupported :CollectionUsageThreshold :CollectionUsageThresholdExceeded :CollectionUsageThresholdCount :CollectionUsage :CollectionUsageThresholdSupported :ObjectName)

; Read an attribute from MBean
jmxlearn.core=> (jmx/read "java.lang:name=Metaspace,type=MemoryPool" :Type)
"NON_HEAP"

; Explore the value of Attributes. Some are a single-values, some are dictionaries(a.k.a Java Composite Type), some are not readable.
jmxlearn.core=> (for [attr (jmx/attribute-names "java.lang:name=Metaspace,type=MemoryPool")]
           #_=>   (try
           #_=>     (println attr "  :  " (jmx/read "java.lang:name=Metaspace,type=MemoryPool" attr))
           #_=>     (catch Exception e (println attr " is not readable"))))
:Name   :   Metaspace
:Type   :   NON_HEAP
:Valid   :   true
:Usage   :   {:committed 22331392, :init 0, :max -1, :used 19890944}
:PeakUsage   :   {:committed 22331392, :init 0, :max -1, :used 19891456}
:MemoryManagerNames   :   #object[[Ljava.lang.String; 0x6cbf8dff [Ljava.lang.String;@6cbf8dff]
:UsageThreshold   :   0
:UsageThresholdExceeded   :   false
:UsageThresholdCount   :   0
:UsageThresholdSupported   :   true
:CollectionUsageThreshold  is not readable
:CollectionUsageThresholdExceeded  is not readable
:CollectionUsageThresholdCount  is not readable
:CollectionUsage   :   nil
:CollectionUsageThresholdSupported   :   false
:ObjectName   :   #object[javax.management.ObjectName 0x268c60c2 java.lang:type=MemoryPool,name=Metaspace]
```
