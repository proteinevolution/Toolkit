FROM openjdk:8u171

# Env variables
ENV SCALA_VERSION 2.12.6
ENV SBT_VERSION 1.1.6
# Override toolkit variables
ENV SUBMIT_MODE "LOCAL"
ENV MAXMIND_DB "/root/GeoLite2-City.mmdb"
ENV MONGODB_URI "mongodb://localhost:27017/tkplay_dev"

# Scala expects this file
RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

# Install Scala
RUN \
    curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
    echo >> /root/.bashrc && \
    echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

# Install sbt
RUN \
    curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
    dpkg -i sbt-$SBT_VERSION.deb && \
    rm sbt-$SBT_VERSION.deb && \
    apt-get update && \
    apt-get install sbt && \
    sbt sbtVersion

# Install custom maxmind geoip
WORKDIR /root
RUN \
    git clone https://github.com/felixgabler/maxmind-geoip2-scala.git && \
    cd maxmind-geoip2-scala && \
    sbt publishLocal

# Install custom scalajs mithril
RUN \
    git clone https://github.com/zy4/scalajs-mithril.git && \
    cd scalajs-mithril && \
    sbt publishLocal

# Download maxmind geoip data
RUN curl -fsL http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.mmdb.gz | gunzip -c > $MAXMIND_DB

VOLUME /app
WORKDIR /app

EXPOSE 1234
CMD sbt "run 1234"
