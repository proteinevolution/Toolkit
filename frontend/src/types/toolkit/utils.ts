export interface AccordionItem {
    title: string;
    content: string;
}

export interface ModalParams {
    id: string; // the id of the modal to be shown as passed to the BaseModal
    props: Record<string, unknown> | undefined; // optional modal props to be set in App.vue before showing the modal
}
