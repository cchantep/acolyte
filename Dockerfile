# syntax=docker/dockerfile:1.7

FROM sbtscala/scala-sbt:eclipse-temurin-17.0.13_11_1.10.7_2.13.16 AS build
WORKDIR /app
COPY project project
COPY build.sbt ./
COPY conf conf
COPY app app
COPY public public
RUN sbt -batch update stage \
 && chmod -R a+rX /app/target/universal/stage \
 && chmod a+x /app/target/universal/stage/bin/play-demo

FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/docker
COPY --from=build /app/target/universal/stage/ ./
RUN chmod -R a+rX /opt/docker \
 && chmod a+x /opt/docker/bin/play-demo
ENV PORT=8000
EXPOSE 8000
USER 1000:1000
ENTRYPOINT ["/bin/sh", "-c", "exec ./bin/play-demo -Dhttp.address=0.0.0.0 -Dhttp.port=${PORT:-8000} -Dpidfile.path=/dev/null"]
