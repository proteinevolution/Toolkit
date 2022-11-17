export const isNullable = (value: any): value is null | undefined => value === null || value === undefined;

export const isNonNullable = <T>(value: T): value is Exclude<T, null | undefined> => !isNullable(value);
