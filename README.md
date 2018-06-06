# jmx-dump
Dumping MBeans info from a JMX server

## Usage
Start a java process with jmx.remote enabled, as target of JMX dumping

    $ SERVER_JVMFLAGS="-Dcom.sun.management.jmxremote.port=12345 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=127.0.0.1" java -jar run-something.jar

Setup environment variable for jmx-dump to find target jvm process:

It can be either JMX_HOST and JMX_PORT (with JMX_USER and JMX_PASSWORD optional)

    $ export JMX_HOST="127.0.0.1" JMX_PORT=12345

Or JMX_PID

    $ export JMX_PID="pid of target java process"

Run

    $ lein run
    JMX_HOST is 127.0.0.1 and JMX_PORT is 12345
    MBean: JMImplementation:type=MBeanServerDelegate
    Attributes: (:MBeanServerId ...
      :MBeanServerId 4c1093a1-96af-4f59-a960-c75127ead712_1446185255866
      :SpecificationName Java Management Extensions
      :SpecificationVersion 1.4
    ...

Build standalone jarfile and run

    $ lein uberjar
    $ java -jar /path/to/jmx-dump-VER-standalone.jar

## Environment Variables

    JMX_PID: The pid of JMX endpoint.
    JMX_HOST: The host of JMX endpoint.
    JMX_PORT: The port of JMX endpoint.
    JMX_USER (optional): User for authentication.
    JMX_PASSWORD (optional): Password for authentication.
    JMX_QUERY (optional): If not set, '*:*' are used.
    JMX_NOCOLOR (optional): If set, colorized output will be disabled.

## Limitation
A function used in the code is removed in java 9. So this can only be run by java 8

## Author
Weichu Liu (@weichu)

## License
The code of `jmx-dump` is distributed under Beerware License:
```
----------------------------------------------------------------------------
"THE BEER-WARE LICENSE" (Revision 42):
<weichu.liu@rakuten.com> wrote this file.  As long as you retain this notice
you can do whatever you want with this stuff. If we meet some day, and you
think this stuff is worth it, you can buy me a beer in return.   -Weichu Liu
----------------------------------------------------------------------------
```
