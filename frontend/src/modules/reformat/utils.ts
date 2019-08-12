export function formatLongSeq(seq: string): string {
    const split = seq.match(/.{1,60}/g);
    if (split === null) {
        return '';
    }
    return split.reduce((prev: string, currentValue: string, currentIndex: number) =>
        prev + currentValue + (currentIndex < split.length - 1 ? '\n' : ''), '');
}
