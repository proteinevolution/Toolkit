

# Model for the Job currently loaded
# TODO Refactor this model
# Information about the current Job
window.JobModel =
  isJob: m.prop false
  jobID: m.prop null
  jobstate: m.prop null
  ownerName: m.prop null
  messages : m.prop []             # All messages that were received with the WebSocket
  executionTime : 0         # Seconds
  createdOn: m.prop null
  tool: m.prop null
  alignmentPresent: false
  views:  m.prop false
  paramValues : {}
  defaultValues:
    "num_iter": 1
    "evalue": 10
    "inclusion_ethresh" : "0.001"
    "min_cov" : "20"
    "msageneration" : "hhblits"
    "gap_open": 11
    "gap_ext": 1
    "desc": 500
    "matrix": "BLOSUM62"
    "max_lines" : 100 # TODO default values can be tool specific (solution for now: name the params tool specific like gap_open_psiblast)
    "pmin" : 20
    "aliwidth" : 80

  pushMessage: (msg) ->
    messages().push(msg)


  update: (args, value) ->
    if args.isJob
      m.request({method: 'GET', url: "/api/job/#{value}"}).then (data) ->
        JobModel.paramValues = data.paramValues
        mainID: data.mainID
        tool : data.toolitem
        isJob: true
        jobID : m.prop data.jobID
        ownerName : m.prop data.ownerName
        createdOn : m.prop data.createdOn
        jobstate :  data.state
        views : data.views
    else
       m.request({method: 'GET', url: "/api/tools/#{value}"}).then (toolitem) ->
        JobModel.paramValues = {}
        tool : toolitem
        isJob: false
        jobID: m.prop ""

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


