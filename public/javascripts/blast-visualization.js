function selectFromArray(checkboxes){
    _.range(1, numHits+1).forEach(function (currentVal) {
        $('input:checkbox[value='+currentVal+'][name="alignment_elem"]').prop('checked', checkboxes.indexOf(currentVal) !== -1);
    })
}

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
