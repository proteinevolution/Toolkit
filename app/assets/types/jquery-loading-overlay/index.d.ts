/// <reference path="../jquery/index.d.ts" />
interface JQuery {
    LoadingOverlay(action?: string, option?: boolean): any;
}

interface JQueryStatic {
    LoadingOverlay(action?: string, option?: boolean): void;
    LoadingOverlaySetup(setting?: any): void;
}