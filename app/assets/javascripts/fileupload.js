/**
 * Represents the WebWorker for efficient file upload using the WebSocket

 * Created by lukas on 1/11/16.
 */

// 1MB chunk sizes.
//const BYTES_PER_CHUNK = 1024 * 1024;

/**
 * Sends the selected chunk over the websocket
 * @param f: file to be uploaded
 * @param ws: Websocket to send the chunks
 */
function process(f, ws) {

    const SIZE = f.size;
    var start = 0;
    var end = BYTES_PER_CHUNK;

    while (start < SIZE) {

        var chunk;

        if ('mozSlice' in f) {
                chunk = f.mozSlice(start, end);
        } else {
                chunk = f.webkitSlice(start, end);
        }


        //ws.send(chunk);

        start = end;
        end = start + BYTES_PER_CHUNK;
        }
}


/*
<script>
function readBlob(opt_startByte, opt_stopByte) {


    // Fetch the file from the input field
    var files = document.getElementById('files').files;
    if (!files.length) {
        alert('Please select a file!');
        return;
    }

    var reader = new FileReader();

    // If we use onloadend, we need to check the readyState.
    reader.onloadend = function(evt) {
        if (evt.target.readyState == FileReader.DONE) { // DONE == 2
            document.getElementById('byte_content').textContent = evt.target.result;
            document.getElementById('byte_range').textContent =
                ['Read bytes: ', start + 1, ' - ', stop + 1,
                    ' of ', file.size, ' byte file'].join('');
        }
    };

    var blob = file.slice(start, stop + 1);
    reader.readAsBinaryString(blob);
}

document.querySelector('.readBytesButtons').addEventListener('click', function(evt) {
    if (evt.target.tagName.toLowerCase() == 'button') {
        var startByte = evt.target.getAttribute('data-startbyte');
        var endByte = evt.target.getAttribute('data-endbyte');
        readBlob(startByte, endByte);
    }
}, false);
</script>
    */