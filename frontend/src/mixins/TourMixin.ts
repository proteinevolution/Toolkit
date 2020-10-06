import Vue from 'vue';

const TourMixin = Vue.extend({
    name: 'myTour',
    data() {
        return {
            steps: [
                {
                    target: '[data-v-step="0"]',  // We're using document.querySelector() under the hood
                    header: {
                        title: 'Toolbar',
                    },
                    content: `These are the different tools you can use`
                },
                {
                    target: '[data-v-step="1"]',
                    content: `Let's checkout this tool. (don't click on the button yourself. click next instead to trigger a bug`
                },
                {
                    target: '[data-v-step="2"]',
                    content: 'Paste an example. Problem: the promise is resolved after router.push but the element referenced in this step has not loaded yet',
                    params: {
                        placement: 'top' // Any valid Popper.js placement. See https://popper.js.org/popper-documentation.html#Popper.placements
                    },
                    before: () => new Promise((resolve, reject) => {
                        this.$router.push('/tools/hhblits');
                        resolve();
                    })
                },
                {
                    target: '[data-v-step="2"]',
                    content: `Paste an example`
                },
                {
                    target: '[data-v-step="3"]',
                    content: `Submit your job`
                },
            ]
        }
    },
    mounted: function () {
        setTimeout(this.$tours['myTour'].start, 5000);
    }
});

export default TourMixin;
