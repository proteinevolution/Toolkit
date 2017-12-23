// file for config of js plugins

$(document).ready(function(){
    // Foundation settings
    Foundation.Tooltip.defaults.clickOpen = false;
    Foundation.Tooltip.defaults.position = "bottom";
    Foundation.Tooltip.defaults.alignment = "center";
    Foundation.Tooltip.defaults.allowOverlap = true;
    $(document).foundation();
    $('#message-container').delay(2000).fadeOut('slow');
});