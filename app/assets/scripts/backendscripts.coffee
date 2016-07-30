GetClock = ->
  d = new Date
  nday = d.getDay()
  nmonth = d.getMonth()
  ndate = d.getDate()
  nyear = d.getYear()
  if nyear < 1000
    nyear += 1900
  nhour = d.getHours()
  nmin = d.getMinutes()
  nsec = d.getSeconds()
  ap = undefined
  if nhour == 0
    ap = ' AM'
    nhour = 12
  else if nhour < 12
    ap = ' AM'
  else if nhour == 12
    ap = ' PM'
  else if nhour > 12
    ap = ' PM'
    nhour -= 12
  if nmin <= 9
    nmin = '0' + nmin
  if nsec <= 9
    nsec = '0' + nsec
  document.getElementById('clockbox').innerHTML = '' + tday[nday] + ', ' + tmonth[nmonth] + ' ' + ndate + ', ' + nyear + ' ' + nhour + ':' + nmin + ':' + nsec + ap + ''
  return

addNewItem = (list) ->
  listItem = document.createElement('li')
  list.appendChild listItem
  listItem.innerText = document.getElementById('todo-textfield').value
  return

tday = new Array('Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')
tmonth = new Array('January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December')

window.onload = ->
  GetClock()
  setInterval GetClock, 1000
  return

document.getElementById('todo-textfield').onkeypress = (e) ->
  if e.keyCode == 13
    addNewItem document.getElementById('todo-items')
    document.getElementById('todo-textfield').value = ''
  return
