let jobNoteArea = function(elem : any, isInit : boolean) : any {
    if (!isInit && $(elem).attr('id').substring(7).length > -1) {
        $.ajax({
            url: '/api/jobs/getnotes/' + $(elem).attr('id').substring(7),
            type: 'get',
            success: function(data) {
                if(data && data.length > 0){
                    $("#notesTab").addClass("hasNotes");
                } else {
                    $("#notesTab").removeClass("hasNotes");
                }
                $(elem).html(data);
            },
            error: function(e){
                console.warn(JSON.stringify(e));
            }
        });
        return $(elem).keyup(function(e) {
            let contentString;
            $("#notesTab").addClass("hasNotes");
            if($(elem).val().length === 0)
                $("#notesTab").removeClass("hasNotes");
            contentString = $(this).val();
            $.post(jsRoutes.controllers.Jobs.annotation($(this).attr('id').substring(7), contentString), function(response) {
                console.log('Response: ' + response);
            });
        });
    }
};