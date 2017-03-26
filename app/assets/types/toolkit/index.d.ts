import ControllerFunction = Mithril.ControllerFunction;


declare class JobListComponent {

    public static reloadList() : any;
    public static lastUpdatedJob : any;
    public static removeJob(param : any) : any;
    public static pushJob(param : any) : any;
    public static pushJob(param: any, bool : boolean) : any;
    public static Job(param : any) : any;
    public static selectedJobID : any;
    public static contains(jobID : string) : any;

    public static controller : any;
    public static view : any;

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

