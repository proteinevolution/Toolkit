#!/usr/bin/env bash
# usage: update_job.sh id

curl -X POST http://localhost:8555/jobs/update/$1
