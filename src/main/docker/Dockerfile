FROM aemdesign/java-ffmpeg

COPY ./build/libs/subtitles-generator-0.0.1-SNAPSHOT.jar /app/app.jar
COPY /speech.json /app/speech.json
COPY ./src/main/docker/wait-for-it.sh /wait-for-it.sh

RUN chmod +x /wait-for-it.sh