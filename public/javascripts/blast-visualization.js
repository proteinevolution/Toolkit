function download(filename, text) {
    var blob = new Blob([text], {type: "octet/stream"});
    if (window.navigator.msSaveOrOpenBlob) {
        window.navigator.msSaveBlob(blob, filename);
    } else {
        var a = document.createElement("a");
        a.style = "display: none";
        a.href = window.URL.createObjectURL(blob);
        a.download = filename;
        // Append anchor to body.
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(a.href);
        // Remove anchor from body
        a.remove();
    }
    $.LoadingOverlay("hide");
}


// load forwarded data into alignment field
$(document).ready(function () {
    var resultcookie = localStorage.getItem("resultcookie");
    $('#alignment').val(resultcookie);
    localStorage.removeItem("resultcookie");
    $.LoadingOverlay("hide");
});


function selectFromArray(checkboxes){
    _.range(1, numHits+1).forEach(function (currentVal) {
        $('input:checkbox[value='+currentVal+'][name="alignment_elem"]').prop('checked', checkboxes.indexOf(currentVal) !== -1);
    })
}

function hitlistBaseFunctions(){
    $(document).ready(function() {
        // add tooltipster to visualization
        $('#blastviz').find('area').tooltipster({
            theme: ['tooltipster-borderless', 'tooltipster-borderless-customized'],
            position: 'bottom',
            animation: 'fade',
            contentAsHTML: true,
            debug: false,
            maxWidth: $(this).innerWidth() * 0.6
        });
        $.LoadingOverlay("hide");
        followScroll(document);

        // add slider val
        $('.slider').on('moved.zf.slider', function () {
            $('#lefthandle').html($('#hidden1').val()).css({
                'color': 'white',
                'font-weight': 'bold',
                'padding-left': '2px'
            });
            $('#righthandle').html($('#hidden2').val()).css({
                'color': 'white',
                'font-weight': 'bold',
                'padding-left': '2px'
            });
        });
    });
}

Array.prototype.removeDuplicates = function () {
    return this.filter(function (item, index, self) {
        return self.indexOf(item) === index;
    });
};

function linkCheckboxes() {
    $('input:checkbox').on('change', function (e) {
        var currentVal = $(this).val();
        var currentState = $(this).prop('checked');

        // link checkboxes with same value
        $('input:checkbox[value=' + currentVal + '][name=alignment_elem]').each(function () {
            $(this).prop('checked', currentState);
        });

        if (currentState) {
            // push num of checked checkbox into array
            checkboxes.push(parseInt(currentVal));
            // make sure array contains no duplicates
            checkboxes = checkboxes.filter(function (value, index, array) {
                return array.indexOf(value) === index;
            });
        } else {
            // delete num of unchecked checkbox from array
            checkboxes = checkboxes.filter(function (x) {
                return x !== currentVal
            });
        }

    }); 
}
