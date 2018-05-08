// file for config of js plugins

$(function(){
    $('#message-container').delay(2000).fadeOut('slow');
});

// Foundation settings
Foundation.Tooltip.defaults.clickOpen = false;
Foundation.Tooltip.defaults.position = "bottom";
Foundation.Tooltip.defaults.alignment = "center";
Foundation.Tooltip.defaults.allowOverlap = true;
$(document).foundation();

$.LoadingOverlaySetup({
    imageColor: "rgba(40,120,111,0.636)",
    size: 13,
    imageAnimation: "2300ms rotate_right"
});