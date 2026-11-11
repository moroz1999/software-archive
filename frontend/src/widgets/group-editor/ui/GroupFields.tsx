import {useTranslation} from 'react-i18next';
import {useGroupForm} from '@/entities/groups';
import {LABEL_NAME_PROBLEM_KEYS, LabelsEditor} from '@/entities/labels';
import {COUNTRY_REQUIRED_KEY, LocationsEditor} from '@/entities/locations';

/** The labels of the group in the form around. */
export function GroupLabels() {
    const {t} = useTranslation();
    const {values, errors, refusals, changeLabels, busy} = useGroupForm();
    return (
        <LabelsEditor
            value={values.labels}
            onChange={changeLabels}
            rowErrors={errors.labels?.map((problem) => (problem === null ? undefined : t(LABEL_NAME_PROBLEM_KEYS[problem])))}
            error={refusals.labels}
            disabled={busy}
        />
    );
}

/** The locations of the group in the form around. */
export function GroupLocations() {
    const {t} = useTranslation();
    const {values, errors, refusals, changeLocations, busy} = useGroupForm();
    return (
        <LocationsEditor
            value={values.locations}
            onChange={changeLocations}
            rowErrors={errors.locations?.map((problem) => (problem === null ? undefined : t(COUNTRY_REQUIRED_KEY)))}
            error={refusals.locations}
            disabled={busy}
        />
    );
}
