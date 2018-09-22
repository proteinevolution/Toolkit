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
    beforeUpdate() {
        this.$title.refresh();
    },
    beforeDestroy() {
        this.$title.addon('');
    },
});

export default hasHTMLTitle;
