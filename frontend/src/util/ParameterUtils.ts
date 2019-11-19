import {
    BooleanParameter, HHpredSelectsParameter,
    NumberParameter,
    ParameterSection,
    SelectParameter,
} from '@/types/toolkit/tools';
import {ParameterType} from '@/types/toolkit/enums';


export function calculateRememberedParameters(submission: any, sections: ParameterSection[]): any {
    const rememberedParams: any = {};

    for (const section of sections) {
        for (const param of section.parameters) {
            const name = param.name;

            switch (param.parameterType) {
                // Only remember parameters that differ from the default values
                case ParameterType.BooleanParameter:
                    const bP = param as BooleanParameter;
                    if (submission.hasOwnProperty(name) && submission[name] !== bP.default) {
                        rememberedParams[name] = submission[name];
                    }
                    break;

                case ParameterType.NumberParameter:
                    const nP = param as NumberParameter;
                    if (submission.hasOwnProperty(name) && submission[name] !== nP.default) {
                        rememberedParams[name] = submission[name];
                    }
                    break;

                case ParameterType.SelectParameter:
                    const sP = param as SelectParameter;
                    if (submission.hasOwnProperty(name) && submission[name] !== sP.default) {
                        rememberedParams[name] = submission[name];
                    }
                    break;

                case ParameterType.TextInputParameter:
                    if (submission.hasOwnProperty(name) && submission[name] !== '') {
                        rememberedParams[name] = submission[name];
                    }
                    break;

                case ParameterType.HHpredSelectsParameter:
                    const hhP = param as HHpredSelectsParameter;
                    if (submission.hasOwnProperty(hhP.name) && submission[hhP.name] !== hhP.default) {
                        rememberedParams[hhP.name] = submission[hhP.name];
                    }
                    if (submission.hasOwnProperty(hhP.nameProteomes) &&
                        submission[hhP.nameProteomes] !== hhP.defaultProteomes) {
                        rememberedParams[hhP.nameProteomes] = submission[hhP.nameProteomes];
                    }
                    break;

                default:
                    // Don't remember parameters for other parameter types.
                    break;
            }
        }
    }
    return rememberedParams;
}
