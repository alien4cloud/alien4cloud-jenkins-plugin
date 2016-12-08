#!/usr/bin/env bash


source ${utils_scripts}/utils.sh

log begin

ensure_home_var_is_set
lock "$(basename $0)"
# Ensure that we will release the lock whatever may happen
trap "unlock $(basename $0)" EXIT

if [[ -e "${STARLINGS_DIR}/.${SOURCE_NODE}-postconfigureLogstashFlag" ]]; then
    log info "Component '${SOURCE_NODE}' already configured to work with Logstash"
    exit 0
fi
install_dir=${HOME}/${SOURCE_NODE}
config_file=${install_dir}/*beat.yml

if [[ "$(grep -c "#LOGSTASH_OUTPUT_PLACEHOLDER#" ${config_file})" != "0" ]]; then
    sed -i -e '/#LOGSTASH_OUTPUT_PLACEHOLDER#/ a \
  logstash:\
    # The Logstash hosts\
    hosts: ["'"$(get_multi_instances_attribute "TARGET_IP_ADDRESS" "TARGET" | sed -e ':a;N;$!ba;s/\n/:5044","/g')"':5044"]\
    loadbalance: true' ${config_file}
fi

touch ${STARLINGS_DIR}/.${SOURCE_NODE}-postconfigureLogstashFlag
log end
