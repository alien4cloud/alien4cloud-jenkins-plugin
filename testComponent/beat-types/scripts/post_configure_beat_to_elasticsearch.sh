#!/usr/bin/env bash


source ${utils_scripts}/utils.sh

log begin

ensure_home_var_is_set

lock "$(basename $0)"
# Ensure that we will release the lock whatever may happen
trap "unlock $(basename $0)" EXIT

if [[ -e "${STARLINGS_DIR}/.${SOURCE_NODE}-postconfigureElasticsearchFlag" ]]; then
    log info "Component '${SOURCE_NODE}' already configured to work with elasticsearch"
    exit 0
fi
install_dir=${HOME}/${SOURCE_NODE}
config_file=${install_dir}/*beat.yml
template_file=$(ls ${install_dir}/*beat.template.json 2> /dev/null | head -1)

# If we can resolve ES using consul lets use this address otherwise assume that ES is running locally
elastic_host="localhost"
if host elasticsearch.service.starlings > /dev/null 2>&1 ; then
    elastic_host="elasticsearch.service.starlings"
fi

beatname=${template_file##*/}
beatname=${beatname%%.*}

if [[ "$(grep -c "#ELASTIC_SEARCH_OUTPUT_PLACEHOLDER#" ${config_file})" != "0" ]]; then

    es_output='  elasticsearch:\
    # Array of hosts to connect to.\
    # Scheme and port can be left out and will be set to the default (http and 9200)\
    hosts: ["'"${elastic_host}"'"]'
    if [[ -f ${template_file} ]] ; then
        es_output="${es_output}"'\
    template:\
      name: "'"${beatname}"'"\
      path: "'"${template_file}"'"\
      overwrite: true'
    fi
    sed -i -e '/#ELASTIC_SEARCH_OUTPUT_PLACEHOLDER#/ a \
'"${es_output}" ${config_file}
fi

# load template directly
#curl -XPUT "http://${elastic_host}:9200/_template/${beatname}" -d@${template_file}

touch ${STARLINGS_DIR}/.${SOURCE_NODE}-postconfigureElasticsearchFlag
log end
