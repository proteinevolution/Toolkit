// file for config of js plugins

$(document).ready(function(){

    // slick navigation slider
    let slickConf = {infinite: false, slidesToShow: 7, slidesToScroll: 0, variableWidth: true, arrows: false };
    $('.slicky').slick(slickConf);
    $(document).foundation();
    $('.tooltipster').tooltipster();
    $('#message-container').delay(2000).fadeOut('slow');
});