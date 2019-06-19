import Vue from 'vue';
import VueI18n from 'vue-i18n';
import messages from './lang/en';
import moment from 'moment';
import mergeWith from 'lodash-es/mergeWith';
import Logger from 'js-logger';

export const defaultLanguage = 'en';
Vue.use(VueI18n);

const logger = Logger.get('i18n');

const i18n = new VueI18n({
    locale: defaultLanguage, // set locale
    fallbackLocale: defaultLanguage,
    messages, // set locale messages
    silentTranslationWarn: process.env.NODE_ENV === 'production',
});

export const possibleLanguages: string[] = [defaultLanguage, 'de'];
const loadedLanguages: string[] = [defaultLanguage]; // our default language that is preloaded
const loadedExtraTranslations: string[] = [];

function setI18nLanguage(lang: string) {
    i18n.locale = lang;
    moment.locale(lang);
    const html = document.querySelector('html');
    if (html != null) {
        html.setAttribute('lang', lang);
    }
    return lang;
}

export function loadLanguageAsync(lang: string) {
    if (i18n.locale !== lang) {
        if (!loadedLanguages.includes(lang)) {
            return import(
                /* webpackChunkName: "lang-[request]",
                 webpackMode: "lazy",
                 webpackPrefetch: false,
                 webpackPreload: false */
                `./lang/${lang}`)
                .then((msgs) => {
                    i18n.setLocaleMessage(lang, msgs.default[lang]);
                    loadedLanguages.push(lang);
                    return setI18nLanguage(lang);
                });
        }
        return Promise.resolve(setI18nLanguage(lang));
    }
    return Promise.resolve(lang);
}

export function loadExtraTranslations(path: string) {
    if (!loadedExtraTranslations.includes(path)) {
        logger.info('loading extra translations for ' + path);
        return import(
            /* webpackChunkName: "lang-extra-[request]",
             webpackMode: "lazy",
             webpackPrefetch: false,
             webpackPreload: false */
            `./lang/extras/${path}.ts`)
            .then((msgs) => {
                for (const itemLang in msgs.default) {
                    if (msgs.default.hasOwnProperty(itemLang)) {
                        const itemMsgs = msgs.default[itemLang];
                        const curMsgs = i18n.getLocaleMessage(itemLang);
                        const newMsgs = mergeWith(curMsgs, itemMsgs);
                        i18n.setLocaleMessage(itemLang, newMsgs);
                    }
                }
                loadedExtraTranslations.push(path);
            });
    }
    return Promise.resolve();
}

export default i18n;
