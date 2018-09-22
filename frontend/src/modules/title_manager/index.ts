import {VueConstructor} from 'vue';
import {TitleProperties} from './types';

const titleProperties: TitleProperties = {
    base: document.title,
    addon: '',
    alert: false,
};

const TitleManager = {
    install(vconst: VueConstructor, args: any = {}) {
        vconst.prototype.$title = {
            addon(value: string) {
                titleProperties.addon = value;
                this.refresh();
            },
            alert(value: boolean) {
                titleProperties.alert = value;
                this.refresh();
            },
            refresh() {
                let res: string = titleProperties.base;
                if (titleProperties.addon) {
                    res = titleProperties.addon + ' | ' + res;
                }
                if (titleProperties.alert) {
                    res = '(*) ' + res;
                }
                document.title = res;
            },
        };
    },
};

export default TitleManager;
