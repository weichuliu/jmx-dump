# Collectd GenerixJMX configuration

This tutorial is following "JMX Instrumentation Introduction"

This is the doc explains the configuration of collectd/GenericJMX.

The Plugin compilation and the connection from collectd to JMX server will be skipped. The official doc is [Plugin:GenericJMX/Config](https://collectd.org/wiki/index.php/Plugin:GenericJMX/Config) and [Plugin:GenericJMX](https://collectd.org/wiki/index.php/Plugin:GenericJMX)
Also you can find this [Config example](https://github.com/collectd/collectd/blob/master/contrib/GenericJMX.conf) in the official collectd source repo. With it you do get some metrics from collectd, however it's kinda hard to understand why it's like that and how to make your own conf. This article will to explain about the configuration.

Here's the basic skeleton of GenerixJMX configuration:

```
LoadPlugin java
<Plugin "java">
  JVMARG "-Djava.class.path=(*^ω^)/java/collectd-api.jar:(*^ω^)/java/generic-jmx.jar"
  LoadPlugin "org.collectd.java.GenericJMX"

  <Plugin "GenericJMX">
    <MBean "">                # MBean block name, which is specified in the following Connection/Collect part
      ObjectName "?"          # Query pattern sending to JMX server
      InstanceFrom "?"        # InstanceFrom can be more than one, the value should be one of the key
                              # Since query are likely to return multiple MBeans
                              # "InstanceFrom" are used to distinguish the source of metrics from same query
      InstancePrefix "?"      # All metrics from this MBean block will be prefixed by

      <Value>                 # There could be multiple Value blocks inside a MBean blocks
        Attribute "?"         # Attribute to query
        Type "?"              # Each metrics must be a type registered in collectd's typedb
        InstancePrefix "?"    # prefix
        InstanceFrom "?"      # Same as the MBean/InstanceFrom but different position
        Table false           # true if the value of the Attribute is a table, collectd will construct multiple metrics
      </Value>
    </MBean>

    <Connection>
      ServiceURL "(*^ω^)"
      Collect ""       # Multi Collect lines to collect Mbeans above
    </Connection>
  </Plugin>
</Plugin>
```

So you loadPlugin, in `<Plugin "GenericJMX">`, there seveal `<MBean "mbean-name">` blocks and one `<Connection>` block. In a MBean block, there are some settings, severel `<Value>` blocks. That's it.
If you correctly set a MBean, then `Collect "mbean-name"` in `Connection`, collectd will get you the data from those MBeans.
Let's look into a `<MBean>` block.

### MBean Block Setting
A MBean block consists of 3 settings:
- `ObjectName`: Corresponding to `jmx/mbean-names` (the method described in "JMX Instrumentation Introduction" document), the query string for MBeans. The number of matched MBeans can be either one or many. When multiple, `InstanceFrom` has to be set.
- `InstanceFrom`: Corresponding to `.GetProperty` of MBean object, one of the *key* of MBeans that's returned by the above query. It's put in metric to distinguish the data from different MBeans. If not set then "". Usually "name" or "type". Refer to the *Collectd Metric Naming Schema*
- `InstancePrefix`: Prefix, usually to distinguish a mbean block from others.

### Value Block Setting
A Value block consists of 5 settings:
- `Type`: A metric in collectd must be contained in [Collect's types.db](https://github.com/collectd/collectd/blob/master/src/types.db). The type name will also show in metric path.
- `Attribute`: Corresponding to `jmx/read`. The Attribute name of the MBean(s). If the attribute is a dict, either use `attr.key1` to access the data, or set Table true to get every key/values.
- `Table`: If value of attribute is a dict or not. If set true, GenericJMX will get every key/value from this attribute.
- `InstancePrefix`: prefix
- `InstanceFrom`: prefix

### Collectd Metric Naming Schema
The naming schema of metric path constructed from MBean configuration is like the following:

```
GenericJMX-<MBean/InstancePrefix>-<MBean/InstanceFrom>.<Value/Type>-<Value/InstancePrefix>-<Value/InstanceFrom>-<Value/Attribute>
```

### Example

```
<MBean "garbage_collector">
  ObjectName "java.lang:type=GarbageCollector,*"
  InstancePrefix "gc-"
  InstanceFrom "name"

  <Value>
    Type "invocations"
    #InstancePrefix ""
    #InstanceFrom ""
    Table false
    Attribute "CollectionCount"
  </Value>

  <Value>
    Type "total_time_in_ms"
    InstancePrefix "collection_time"
    #InstanceFrom ""
    Table false
    Attribute "CollectionTime"
  </Value>
</MBean>
```

Here's the `garbage_collection` MBean block.
The query is "java.lang:type=GarbageCollector,*"
The MBeans from the query are

```
; Normal GC
jmxlearn.core=> (map str (jmx/mbean-names "java.lang:type=GarbageCollector,*"))
("java.lang:type=GarbageCollector,name=PS Scavenge" "java.lang:type=GarbageCollector,name=PS MarkSweep")

; G1 Collector, If :jvm-opts ["-XX:+UseG1GC"] is set in project.clj
jmxlearn.core=> (map str (jmx/mbean-names "java.lang:type=GarbageCollector,*"))
("java.lang:type=GarbageCollector,name=G1 Young Generation" "java.lang:type=GarbageCollector,name=G1 Old Generation")
```
Since there are two MBeans with different name, the `MBean/InstanceFrom` must be set. Here it's set to `"name"`

There are two Value blocks, each collecting `"CollectionCount"` and `"CollectionTime"`. Let's read the Attribute:
```
jmxlearn.core=> (jmx/read "java.lang:type=GarbageCollector,name=G1 Young Generation" :CollectionTime)
16733
jmxlearn.core=> (jmx/read "java.lang:type=GarbageCollector,name=G1 Old Generation" :CollectionTime)
0
jmxlearn.core=> (jmx/read "java.lang:type=GarbageCollector,name=G1 Young Generation" :CollectionCount)
17
jmxlearn.core=> (jmx/read "java.lang:type=GarbageCollector,name=G1 Old Generation" :CollectionCount)
0
```

Each MBean returns a CollectionTime/CollectionCount value.

Finally, with the above configuration, we will get following metrics (dump by write_log plugin, note that it shows G1_Old_Generation because space are escaped. If it was sent by write_kafka plugin and JSON format, spaces will remain in the path):

```
GenericJMX-gc-G1_Old_Generation.invocations 0 1448009145
GenericJMX-gc-G1_Old_Generation.total_time_in_ms-collection_time 0 1448009145
GenericJMX-gc-G1_Young_Generation.invocations 37 1448009145
GenericJMX-gc-G1_Young_Generation.total_time_in_ms-collection_time 338 1448009145
```

So this is how to configure collectd to get metrics from JMX and how to configure collectd to construct path for metrics.


