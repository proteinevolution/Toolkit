import ControllerFunction = Mithril.ControllerFunction;

declare class jsRoutes {
    public static controllers : any;
}

declare class JobListComponent {

    public static reloadList() : any;
    public static lastUpdatedJob : any;
    public static removeJob(param : any, messageServer?: boolean, deleteJob?: boolean) : any;
    public static pushJob(param : any, bool? : boolean) : any;
    public static Job(param : any) : any;
    public static selectedJobID : string;
    public static getJobIndex(jobID : string) : number;
    public static contains(jobID : string) : any;
    public static list: any;
    public static getJob : any;

    public static controller : any;
    public static view : any;
    public static jobIDsFiltered() : any;
    public static register(joblist? : Array<string>): void;
    public static sortList(): boolean;
    public static sort : any;
    public static visibleJobs() : any;
    public static scrollToJobListItem(item : any) : any;
    public static index : number;
    public static numVisibleItems : number;
    public static scrollJobList(scrollItems : any, pos: any) : any;

}

declare class JobLineComponent{
    public static controller: any;
    public static view: any;
}

declare class JobTabsComponent{
    public static controller: any;
    public static view: any;
}

declare class FrontendAlnvizComponent {

    public static controller : any;
    public static view: any;

}

declare class FrontendReformatComponent {}

declare class JobViewComponent {

    public static controller: any;
    public static view: any;

}

declare class JobSubmissionComponent {
    public static submitting      : boolean;
    public static currentJobID    : string;
    public static jobIDValid      : boolean;
    public static jobIDValidationTimeout : number;
    public static jobIDRegExp     : RegExp;
    public static jobResubmit     : boolean;
    public static checkJobID(jobID : string, addResubmitVersion? : boolean) : any;
    public static jobIDComponent(ctrl : any) : any;
    public static hide(ctrl : any, args : any) : any;
    public static controller(args : any) : any;
    public static view (ctrl : any, args : any): any;
}

declare class JobRunningComponent {

    public static controller: any;
    public static view: any;
    public static updateLog: any;

}
declare class Backend{
    public static controller : any;
    public static view: any;
}

declare class Toolkit{
    public static currentJobID : string;
    public static controller : any;
    public static view: any;
    public static isJob : boolean;
}

declare class Index{

    public static controller : any;
    public static view: any;
}

declare class News{

    public static controller : any;
    public static view: any;
}


declare class JobManager{

    public static tableObjects : any;
    public static model : any;
    public static controller : any;
    public static view: any;
    public static reload : Function;
    public static data : any;
    public static dataTableLoader: Function;
    public static toColumnItems: Function;
    public static toColumnNames: Function;

}


interface JobData {
    data : {

        jobID : any;
    }
}

declare class Job {
    jobID : string;
    toolnameLong : string;
    state : Number
}