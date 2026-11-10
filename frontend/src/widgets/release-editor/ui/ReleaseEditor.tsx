import type {ReactNode} from 'react';
import type {UseQueryResult} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {
    type Hardware,
    type ReleaseDetails,
    PlatformHardwareProvider,
    ReleaseDetailsProvider,
    ReleaseForm,
    type ReleaseId,
    type SoftwareDetails,
    SoftwareDetailsProvider,
    SoftwareFormSkeleton,
    type SoftwareId,
    usePlatformHardware,
    useReleaseDetails,
    useSoftwareDetails,
} from '@/entities/software';
import {CreateReleaseProvider} from '@/features/release-create';
import {DeleteReleaseButton} from '@/features/release-delete';
import {EditReleaseProvider} from '@/features/release-edit';
import {useSkeleton} from '@/shared/lib/skeleton';
import {PageLayout} from '@/shared/ui/page-layout';

export interface ReleaseCreatorProps {
    readonly softwareId: SoftwareId;
    /** Called once the release is added. */
    readonly onDone: () => void;
}

/** The page of adding a release to software: its form with the hardware of the software's platform. */
export function ReleaseCreator({softwareId, onDone}: ReleaseCreatorProps) {
    const {t} = useTranslation();
    return (
        <WithSoftwareAndHardware id={softwareId} title={t('release.add')} titleOf={(software) => t('release.addTo', {title: software.title})}>
            {(software, hardware) => (
                <CreateReleaseProvider software={software} hardware={hardware} onCreated={onDone}>
                    <ReleaseForm submitLabel={t('release.add')}/>
                </CreateReleaseProvider>
            )}
        </WithSoftwareAndHardware>
    );
}

export interface ReleaseEditorProps {
    readonly softwareId: SoftwareId;
    readonly releaseId: ReleaseId;
    /** Called once the release is saved or deleted. */
    readonly onDone: () => void;
}

/** The page of editing one release: its form, saving and deleting. */
export function ReleaseEditor({softwareId, releaseId, onDone}: ReleaseEditorProps) {
    const {t} = useTranslation();
    return (
        <WithSoftwareAndHardware id={softwareId} title={t('release.edit')} titleOf={(software) => t('release.editOf', {title: software.title})}>
            {(software, hardware) => (
                <ReleaseDetailsProvider softwareId={softwareId} releaseId={releaseId}>
                    <ReleaseGate>
                        {(release) => (
                            <EditReleaseProvider software={software} release={release} hardware={hardware} onSaved={onDone}>
                                <ReleaseForm submitLabel={t('actions.save')} actions={<DeleteReleaseButton software={software} release={release} onDeleted={onDone}/>}/>
                            </EditReleaseProvider>
                        )}
                    </ReleaseGate>
                </ReleaseDetailsProvider>
            )}
        </WithSoftwareAndHardware>
    );
}

interface WithSoftwareAndHardwareProps {
    readonly id: SoftwareId;
    /** The page title until the software arrives. */
    readonly title: string;
    readonly titleOf: (software: SoftwareDetails) => string;
    readonly children: (software: SoftwareDetails, hardware: readonly Hardware[]) => ReactNode;
}

/** The software and the hardware of its platform, which every form of a release needs; the page is titled by the software. */
function WithSoftwareAndHardware({id, title, titleOf, children}: WithSoftwareAndHardwareProps) {
    return (
        <SoftwareDetailsProvider key={id} id={id}>
            <SoftwareGate title={title}>
                {(software) => (
                    <PageLayout title={titleOf(software)}>
                        <PlatformHardwareProvider platform={software.platform.code}>
                            <HardwareGate>{(hardware) => children(software, hardware)}</HardwareGate>
                        </PlatformHardwareProvider>
                    </PageLayout>
                )}
            </SoftwareGate>
        </SoftwareDetailsProvider>
    );
}

/** The children of a gate: the block shown once its data is there. */
interface GateProps<T> {
    readonly children: (data: T) => ReactNode;
}

function SoftwareGate({title, children}: GateProps<SoftwareDetails> & {readonly title: string}) {
    return <Loaded query={useSoftwareDetails()} title={title}>{children}</Loaded>;
}

function HardwareGate({children}: GateProps<readonly Hardware[]>) {
    return <Loaded query={usePlatformHardware()}>{children}</Loaded>;
}

function ReleaseGate({children}: GateProps<ReleaseDetails>) {
    return <Loaded query={useReleaseDetails()}>{children}</Loaded>;
}

interface LoadedProps<T> {
    readonly query: UseQueryResult<T>;
    /** Set when the block is the page: the skeleton is shown under this title. */
    readonly title?: string;
    readonly children: (data: T) => ReactNode;
}

/** A block of the form shown once its query has data, its skeleton before (docs/data-loading.md). */
function Loaded<T>({query, title, children}: LoadedProps<T>) {
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        const bones = <div data-testid="release-editor-skeleton"><SoftwareFormSkeleton/></div>;
        return title === undefined ? bones : <PageLayout title={title}>{bones}</PageLayout>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return children(query.data);
}
