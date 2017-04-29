#!/bin/bash

JAVA_OPTS="-server -XX:MaxPermSize=2048m -Xms2048m -Xmx2048m -XX:MetaspaceSize=1024m" activator -Dhttp.proxyHost="dmzproxy.local"\
 -Dhttp.proxyPort="8080" -Dhttps.proxyHost="dmzproxy" -Dhttps.proxyPort="8080" -Dhttp.nonProxyHosts="localhost|127.0.0.1"

