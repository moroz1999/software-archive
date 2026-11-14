import {typographyClassName} from '../typography/typography';

/** The star of a required field: seen, not read out, since the control says it is required. */
export function RequiredMark() {
    return <span aria-hidden="true" className={typographyClassName('label', 'danger', false)}>*</span>;
}
