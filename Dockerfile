FROM library/centos:7

ENV JAVA_DISTR_URL="https://cloud-pipeline-oss-builds.s3.amazonaws.com/tools/java/openjdk-11.0.2_linux-x64_bin.tar.gz"
ENV JAVA_HOME="/opt/jdk"
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV BW_HOME=/opt/blast-wrapper
ENV BW_CONFIG_DIR=$BW_HOME/config

RUN cd /tmp && \
    curl -s "$JAVA_DISTR_URL" -o openjdk.tar.gz && \
    tar -zxf openjdk.tar.gz --no-same-owner && \
    rm -f openjdk.tar.gz && \
    mv *jdk* "$JAVA_HOME"

ADD . /blast-wrapper
RUN cd /blast-wrapper && \
    ./gradlew build && \
    mkdir -p ${BW_HOME} && \
    mv /blast-wrapper/build/libs/blast-wrapper.jar ${BW_HOME}/ && \
    rm -rf /blast-wrapper

RUN mkdir -p ${BW_CONFIG_DIR} && \
    echo "blast-wrapper.blast-commands.request-validators.targetSequenceMaxLimit=200"   > ${BW_CONFIG_DIR}/application.properties && \
    echo "blast-wrapper.blast-commands.blast-db-directory=$BW_HOME/blast-db/"           >> ${BW_CONFIG_DIR}/application.properties && \
    echo "blast-wrapper.blast-commands.blast-results-directory=$BW_HOME/results/"       >> ${BW_CONFIG_DIR}/application.properties && \
    echo "blast-wrapper.blast-commands.blast-fasta-directory=$BW_HOME/fasta/"           >> ${BW_CONFIG_DIR}/application.properties && \
    echo "blast-wrapper.blast-commands.blast-queries-directory=$BW_HOME/queries/"       >> ${BW_CONFIG_DIR}/application.properties

EXPOSE 8080
CMD cd $BW_HOME && java -Xmx2g -jar blast-wrapper.jar
