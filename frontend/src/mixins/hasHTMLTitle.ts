import Vue from 'vue';

const hasHTMLTitle = Vue.extend({
    computed: {
        htmlTitle() {
            return '';
        },
    },
    beforeMount() {
        this.$title.addon(this.htmlTitle);
    },
    beforeDestroy() {
        this.$title.addon('');
    },
    watch: {
        htmlTitle(val: string) {
            this.$title.addon(val);
        },
    },
});

export default hasHTMLTitle;
