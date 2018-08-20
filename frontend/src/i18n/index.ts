import Vue from 'vue';
import VueI18n from 'vue-i18n';
import messages from './lang/en';

export const defaultLanguage = 'en';
Vue.use(VueI18n);

export const i18n = new VueI18n({
    locale: defaultLanguage, // set locale
    fallbackLocale: defaultLanguage,
    messages, // set locale messages
    silentTranslationWarn: process.env.NODE_ENV === 'production',
});

export const possibleLanguages = [defaultLanguage, 'de'];
const loadedLanguages = [defaultLanguage]; // our default language that is preloaded

function setI18nLanguage(lang: string) {
    i18n.locale = lang;
    const html = document.querySelector('html');
    if (html != null) {
        html.setAttribute('lang', lang);
    }
    return lang;
}

export function loadLanguageAsync(lang: string) {
    if (i18n.locale !== lang) {
        if (!loadedLanguages.includes(lang)) {
            return import(/* webpackChunkName: "lang-[request]" */ `./lang/${lang}`)
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

