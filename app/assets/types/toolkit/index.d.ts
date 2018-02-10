

declare class jsRoutes {
    public static controllers : any;
}

declare class JobListComponent {

    public static reloadList() : any;
    public static lastUpdatedJob : any;
    public static removeJob(param : any, messageServer?: boolean, deleteJob?: boolean) : any;
    public static pushJob(newJob : any, setActive? : boolean) : any;
    public static Job(param : any) : any;
    public static selectedJobID : string;
    public static currentTool: string;
    public static getJobIndex(jobID : string) : number;
    public static contains(jobID : string) : any;
    public static list: any;
    public static getJob : any;
    public static reloadJob : any;
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
    public static selectJob() : any;

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

declare class Job404Component {

    public static controller: any;
    public static view: any;

}

declare class JobSubmissionComponent {
    public static submitting      : boolean;
    public static oldJobID        : string;
    public static currentJobID    : string;
    public static jobIDValid      : boolean;
    public static jobIDValidationTimeout : number;
    public static jobIDRegExp     : RegExp;
    public static checkJobID(jobID? : string) : any;
    public static checkJobIDTimed(timeout : number) : any;
    public static jobIDComponent(ctrl : any) : any;
    public static hide(ctrl : any, args : any) : any;
    public static controller(args : any) : any;
    public static view (ctrl : any, args : any): any;
}


declare class Backend{
    public static controller : any;
    public static view: any;
}

declare class Toolkit{
    public static currentJobID : string;
    public static trackedJobIDs: Array;
    public static notFoundJobID: string;
    public static controller : any;
    public static view: any;
    public static isJob : boolean;
}

declare class Index{

    public static controller : any;
    public static view: any;
}

declare class JobManager{

    public static model : any;
    public static controller : any;
    public static view: any;
    public static data : any;
    public static table : any;
    public static removeFromList: Function;
    public static addToList: Function;
    public static deleteJob: Function;
    public static dataTables: Function;
    public static getJob: Function;
    public static pushToTable: Function;
    public static removeFromTable: Function;
    public static reload: Function;
    public static dataTableLoader: Function;
    public static dataLoader: Function;


}


interface JobData {
    data : {

        jobID : any;
    }
}

declare class Job {
    jobID : string;
    toolnameLong : string;
    status : Number
}
