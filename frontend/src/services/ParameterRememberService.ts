class ParameterRememberService {

    private KEY = 'remember_parameters';

    public has(toolName: string): boolean {
        return toolName in this.getStorage();
    }

    public save(toolName: string, parameters: Record<string, unknown>): void {
        const storage = this.getStorage();
        storage[toolName] = parameters;
        this.setStorage(storage);
    }

    public load(toolName: string): Record<string, unknown> {
        const storage = this.getStorage();
        if (toolName in storage) {
            return storage[toolName];
        }
        return {};
    }

    public reset(toolName: string): void {
        const storage = this.getStorage();
        delete storage[toolName];
        this.setStorage(storage);
    }

    private getStorage(): any {
        const storage: string | null = localStorage.getItem(this.KEY);
        if (storage) {
            return JSON.parse(storage);
        }
        return {};
    }

    private setStorage(storage: any): void {
        localStorage.setItem(this.KEY, JSON.stringify(storage));
    }
}

export const parameterRememberService = new ParameterRememberService();
