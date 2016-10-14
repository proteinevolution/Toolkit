#!/usr/bin/env bash
# usage: JobStatusDone.sh id

curl -X POST http://localhost:8555/jobs/done/$1
