// Important: import Vue from 'vue';
import Vue from 'vue'; // eslint-disable-line @typescript-eslint/no-unused-vars

interface TitleProperties {
    base: string;
    addon: string;
    alert: boolean;
}

interface TKTitleFunctions {
    addon(value: string): void;
    alert(value: boolean): void;
    refresh(): void;
}

declare module 'vue/types/vue' {
    interface Vue {
        $title: TKTitleFunctions;
    }
}
