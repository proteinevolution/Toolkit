import { createI18n } from 'vue-i18n';
import messages from './lang/en';
import mergeWith from 'lodash-es/mergeWith';
import Logger from 'js-logger';
import { Settings } from 'luxon';

export const defaultLanguage = 'en';
Settings.defaultLocale = defaultLanguage;

const logger = Logger.get('i18n');

const i18n = createI18n({
    legacy: false,
    locale: localStorage.getItem('tk-locale') || defaultLanguage, // set locale
    fallbackLocale: defaultLanguage,
    messages, // set locale messages
    silentTranslationWarn: import.meta.env.PROD,
});

export const possibleLanguages: string[] = [defaultLanguage, 'de'];
const loadedLanguages: string[] = [defaultLanguage]; // our default language that is preloaded
const loadedExtraTranslations: string[] = [];

function setI18nLanguage(lang: PossibleLanguage) {
    if (i18n.mode === 'legacy') {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore we have to wait until vue-i18n fixes the types https://github.com/intlify/vue-i18n-next/issues/1003
        i18n.global.locale = lang;
    } else {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore we have to wait until vue-i18n fixes the types https://github.com/intlify/vue-i18n-next/issues/1003
        i18n.global.locale.value = lang;
    }
    localStorage.setItem('tk-locale', lang);
    Settings.defaultLocale = lang;
    document.querySelector('html')?.setAttribute('lang', lang);
    return lang;
}

export function loadLanguageAsync(lang: string): Promise<string> {
    if (i18n.global.locale.value !== lang && isPossibleLanguage(lang)) {
        if (!loadedLanguages.includes(lang)) {
            return import(`./lang/${lang}.ts`).then((msgs) => {
                i18n.global.setLocaleMessage(lang, msgs.default[lang]);
                loadedLanguages.push(lang);
                return setI18nLanguage(lang);
            });
        }
        return Promise.resolve(setI18nLanguage(lang));
    }
    return Promise.resolve(lang);
}

export async function loadExtraTranslations(path: string): Promise<void> {
    if (!loadedExtraTranslations.includes(path)) {
        logger.info('loading extra translations for ' + path);
        const splitPath = path.split('/');
        let msgs;
        if (splitPath.length === 1) {
            msgs = (await import(`./lang/extras/${splitPath[0]}.ts`)).default;
        }
        if (splitPath.length === 2) {
            msgs = (await import(`./lang/extras/${splitPath[0]}/${splitPath[1]}.ts`)).default;
        }
        if (splitPath.length === 3) {
            msgs = (await import(`./lang/extras/${splitPath[0]}/${splitPath[1]}/${splitPath[2]}.ts`)).default;
        }
        if (splitPath.length === 4) {
            msgs = (await import(`./lang/extras/${splitPath[0]}/${splitPath[1]}/${splitPath[2]}/${splitPath[3]}.ts`))
                .default;
        }
        for (const itemLang in msgs) {
            if (itemLang in msgs) {
                const itemMsgs = msgs[itemLang];
                const curMsgs = i18n.global.getLocaleMessage(itemLang);
                const newMsgs = mergeWith(curMsgs, itemMsgs);
                i18n.global.setLocaleMessage(itemLang, newMsgs);
            }
        }
        loadedExtraTranslations.push(path);
    }
}

type PossibleLanguage = 'en' | 'de';

function isPossibleLanguage(lang: string): lang is PossibleLanguage {
    return possibleLanguages.includes(lang);
}

export default i18n;
