<template>
    <div id="topnavbar" class="float-right">
        <div id="meta-user" class="float-right"></div>
        <div id="social" class="float-right">
            <b-button variant="href" href="https://github.com/proteinevolution/Toolkit" target="_blank" rel="noopener">
                <i class="fab fa-github"></i></b-button>
            <b-button variant="href" href="https://www.facebook.com/mpitoolkit" target="_blank" rel="noopener"><i
                    class="fab fa-facebook-f"></i></b-button>
            <b-button variant="href" href="https://twitter.com/mpitoolkit" target="_blank" rel="noopener"><i
                    class="fab fa-twitter"></i></b-button>
        </div>

        <div id="offline-alert" class="float-right" onclick="window.location.reload()">
            <i class="fas fa-retweet"></i>
            <b>Reconnecting...</b>
        </div>
        <div class="maintenance-alert float-right">
            <i class="fa fa-wrench"></i>
            <b>Maintenance in a few seconds!</b>
        </div>
    </div>
</template>


<style>
    #topnavbar {
        background-color: #cccccc;
        width: 100%;
    }
</style>