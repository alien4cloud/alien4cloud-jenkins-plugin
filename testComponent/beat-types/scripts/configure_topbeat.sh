#!/usr/bin/env bash

source ${utils_scripts}/utils.sh
log begin

ensure_home_var_is_set

if isServiceConfigured; then
    log info "TopBeat component '${NODE}' already configured"
    exit 0
fi

install_dir=${HOME}/${NODE}
config_file=${install_dir}/topbeat.yml

sed -i -e "s/period:.*$/period: ${PERIOD}/g" ${config_file}

setServiceConfigured
log end
