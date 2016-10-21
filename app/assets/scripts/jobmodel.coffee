

# Model for the Job currently loaded
# TODO Refactor this model
# Information about the current Job
window.JobModel =
  isJob: m.prop false
  jobid: m.prop null
  jobstate: m.prop null
  createdOn: m.prop null
  tool: m.prop null
  alignmentPresent: false
  views:  m.prop false
  paramValues : {}
  defaultValues:
    "num_iter": 1
    "evalue": 10
    "inclusion_ethresh": 0.001
    "gap_open": 11
    "gap_ext": 1
    "desc": 500
    "matrix": "BLOSUM62"

  getTool: (toolname) ->
    m.request {method: 'GET', url: "/api/tools/#{toolname}"}

  getJob: (mainID) ->
    m.request {method: 'GET', url: "/api/jobs/#{mainID}"}

  getParamValue: (param) ->

# Prefer the alignment from the local storage, if found
    resultcookie = localStorage.getItem("resultcookie")
    if resultcookie
      JobModel.paramValues["alignment"] = resultcookie
      localStorage.removeItem("resultcookie")
    val = JobModel.paramValues[param]
    defVal = JobModel.defaultValues[param]
    if val
      val
    else if defVal
      defVal
    else
      ""
