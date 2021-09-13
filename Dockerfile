
FROM library/centos:7

ARG BW_WORKDIR="/opt/blast-wrapper"

ENV JAVA_DISTR_URL="https://cloud-pipeline-oss-builds.s3.amazonaws.com/tools/java/openjdk-11.0.2_linux-x64_bin.tar.gz"
ENV JAVA_HOME="/opt/jdk"
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV BW_HOME=/opt/blast-wrapper
ENV BW_CONFIG_DIR=$BW_HOME/config
ENV BW_CMD_TEMPLATES=$BW_HOME/templates

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
    echo "blast-wrapper.blast-commands.blast-db-directory=$BW_WORKDIR/blast-db/"           >> ${BW_CONFIG_DIR}/application.properties && \
    echo "blast-wrapper.blast-commands.blast-results-directory=$BW_WORKDIR/results/"       >> ${BW_CONFIG_DIR}/application.properties && \
    echo "blast-wrapper.blast-commands.blast-fasta-directory=$BW_WORKDIR/fasta/"           >> ${BW_CONFIG_DIR}/application.properties && \
    echo "blast-wrapper.blast-commands.blast-queries-directory=$BW_WORKDIR/queries/"       >> ${BW_CONFIG_DIR}/application.properties && \
    echo "blast-wrapper.template.command.dir=$BW_CMD_TEMPLATES" >> ${BW_CONFIG_DIR}/application.properties

ADD cp src/main/resources/commands.templates $BW_CMD_TEMPLATES

EXPOSE 8080
CMD cd $BW_HOME && java -Xmx2g -jar blast-wrapper.jar
