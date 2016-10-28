reader = undefined
progress = document.querySelector('.percent')
# Setup the dnd listeners.
#var dropZone = document.getElementById('drop_zone');
#dropZone.addEventListener('dragover', handleDragOver, false);
#dropZone.addEventListener('drop', handleFileSelect, false);

handleFileSelect = (evt) ->
  evt.stopPropagation()
  evt.preventDefault()
  files = evt.dataTransfer.files
  # FileList object.
  # files is a FileList of File objects. List some properties.
  output = []
  progress.style.width = '0%'
  progress.textContent = '0%'
  reader = new FileReader
  i = 0
  f = undefined
  while f = files[i]
    if !f.type.match('text/plain')
      output.push '<li><strong>', 'Please drop only plain text files with *.txt as suffix.', '</strong> </li>'
      i++
      continue
    reader.onerror = errorHandler
    reader.onprogress = updateProgress

    reader.onabort = (e) ->
      alert 'File read cancelled'
      return

    reader.onloadstart = (e) ->
      document.getElementById('progress_bar').className = 'loading'
      return

    reader.onload = (e) ->
      # Ensure that the progress bar displays 100% at the end.
      progress.style.width = '100%'
      progress.textContent = '100%'
      setTimeout 'document.getElementById(\'progress_bar\').className=\'\';', 2000
      return

    # Read in the image file as a binary string.
    reader.readAsBinaryString evt.target.files[i]
    output.push '<li><strong>', escape(f.name), '</strong> (', f.type or 'n/a', ') - ', f.size, ' bytes, last modified: ', f.lastModifiedDate.toLocaleDateString(), '</li>'
    i++
  document.getElementById('list').innerHTML = '<ul>' + output.join('') + '</ul>'
  return

handleFileSelect2 = (evt) ->
  # Reset progress indicator on new file selection.
  progress.style.width = '0%'
  progress.textContent = '0%'
  reader = new FileReader
  reader.onerror = errorHandler
  reader.onprogress = updateProgress

  reader.onabort = (e) ->
    alert 'File read cancelled'
    return

  reader.onloadstart = (e) ->
    document.getElementById('progress_bar').className = 'loading'
    return

  reader.onload = (e) ->
    # Ensure that the progress bar displays 100% at the end.
    progress.style.width = '100%'
    progress.textContent = '100%'
    setTimeout 'document.getElementById(\'progress_bar\').className=\'\';', 2000
    if evt.target.files[0].type.match('text/plain')
      myCodeMirror.setValue reader.resultpanel
    else
      alert 'type mismatch, please upload only plain text files.'
    return

  # Read in the image file as a binary string.
  #reader.readAsBinaryString(evt.target.files[0]);
  reader.readAsText evt.target.files[0]
  return

updateProgress = (evt) ->
  # evt is an ProgressEvent.
  if evt.lengthComputable
    percentLoaded = Math.round(evt.loaded / evt.total * 100)
    # Increase the progress bar length.
    if percentLoaded < 100
      progress.style.width = percentLoaded + '%'
      progress.textContent = percentLoaded + '%'
  return

errorHandler = (evt) ->
  switch evt.target.error.code
    when evt.target.error.NOT_FOUND_ERR
      alert 'File Not Found!'
    when evt.target.error.NOT_READABLE_ERR
      alert 'File is not readable'
    when evt.target.error.ABORT_ERR
    # noop
    else
      alert 'An error occurred reading this file.'
  return

abortRead = ->
  reader.abort()
  return

handleDragOver = (evt) ->
  evt.stopPropagation()
  evt.preventDefault()
  evt.dataTransfer.dropEffect = 'copy'
  # Explicitly show this is a copy.
  return

document.getElementById('files').addEventListener 'change', handleFileSelect2, false

