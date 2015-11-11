# jmx-dump
Dumping MBeans info from a JMX server

## Usage
Set jmx.remote for a java process

    $ SERVER_JVMFLAGS="-Dcom.sun.management.jmxremote.port=12345 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=127.0.0.1" java -jar run-something.jar

Start jmx-dump with JMX_HOST and JMX_PORT

    $ export JMX_HOST="127.0.0.1" JMX_PORT=12345

Run

    $ lein run
    JMX_HOST is 127.0.0.1 and JMX_PORT is 12345
    MBean: JMImplementation:type=MBeanServerDelegate
    Attributes: (:MBeanServerId ...
      :MBeanServerId 4c1093a1-96af-4f59-a960-c75127ead712_1446185255866
      :SpecificationName Java Management Extensions
      :SpecificationVersion 1.4
    ...

## TODO
- Currently the jmx-dump can only connect to an application that with `jmxremote.port` open.
  Might be good to have some other connecting options
- Add some other fancy features one day.


## Author
Weichu Liu (weichu.liu@rakuten.com)

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
