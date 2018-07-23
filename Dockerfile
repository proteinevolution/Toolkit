FROM hseeberger/scala-sbt:8u171_2.12.6_1.1.6

# Override toolkit variables
ENV SUBMIT_MODE "LOCAL"
ENV MAXMIND_DB "/root/GeoLite2-City.mmdb"
ENV MONGODB_URI "mongodb://localhost:27017/tkplay_dev"

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
