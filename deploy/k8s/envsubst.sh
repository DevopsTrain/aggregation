#!/bin/bash
sed -e "s/\${BUILD_NUMBER}/${CI_BUILD_NUMBER}/" deploy-CI.template.yaml  > deploy-CI.yaml