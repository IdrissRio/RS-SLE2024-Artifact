FROM ubuntu:22.04

RUN apt-get update
RUN rm /bin/sh && ln -s /bin/bash /bin/sh

RUN apt-get update && apt-get -y install \
    bash \
    zsh \
    git \
    curl \
    unzip \
    curl \
    vim \
    python3.9 \
    python3-pip\
    openjdk-8-jdk \
    openjfx\
    openjdk-11-jdk \
    openjfx\
    sudo

RUN curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -

RUN apt-get install -y locales locales-all nodejs
ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8
RUN ln -fs /usr/share/zoneinfo/America/New_York /etc/localtime

RUN apt-get -y install \
    texlive-latex-base \
    texlive-latex-extra

RUN useradd -ms /bin/bash SLE2024

RUN zsh

RUN sudo ln -s /usr/bin/python3.9 /usr/bin/python


RUN pip install 'numpy' 'pandas' 'matplotlib' 'seaborn'  'scipy' 'prettytable'
ENV SDKMAN_DIR=/home/SLE2024/.sdkman

RUN curl -s "https://get.sdkman.io" | bash
RUN bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && sdk install java 8.0.412.fx-zulu"
ENV JAVA_HOME="$SDKMAN_DIR/candidates/java/current"
ENV PATH="$JAVA_HOME/bin:~/.sdkman:$PATH"

RUN apt-get install -y jq bc


RUN mkdir -p /data
ADD data /data

RUN echo "SLE2024:SLE2024" | chpasswd && adduser SLE2024 sudo
RUN chown -R SLE2024 ./data
USER SLE2024

