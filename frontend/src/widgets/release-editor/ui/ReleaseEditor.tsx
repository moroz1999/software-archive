import type {ReactNode} from 'react';
import type {UseQueryResult} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {type Hardware, type ReleaseDetails, PlatformHardwareProvider, ReleaseDetailsProvider, ReleaseForm, releasePath, type SoftwareDetails, SoftwareDetailsProvider, SoftwareFormSkeleton, softwarePath, usePlatformHardware, useReleaseDetails, useSoftwareDetails} from '@/entities/software';
import {CreateReleaseProvider} from '@/features/release/create';
import {DeleteReleaseButton} from '@/features/release/delete';
import {EditReleaseProvider} from '@/features/release/edit';
import {ScreenshotsEditorSection} from '@/features/software/screenshots';
import {softwareBreadcrumbs} from '@/features/software/catalog';
import type {Crumb} from '@/shared/ui/breadcrumbs';
import {useSkeleton} from '@/shared/lib/loading';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {Stack} from '@/shared/ui/stack';

export interface ReleaseCreatorProps {
    readonly softwareId: string;
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
                    <ReleaseForm
                        submitLabel={t('release.add')}
                        actions={<Button href={softwarePath(softwareId)} color="secondary">{t('actions.cancel')}</Button>}
                    />
                </CreateReleaseProvider>
            )}
        </WithSoftwareAndHardware>
    );
}

export interface ReleaseEditorProps {
    readonly softwareId: string;
    readonly releaseId: string;
    /** Called once the release is saved or deleted. */
    readonly onDone: () => void;
}

/** The page of editing one release: deleting next to its title, its form, and its screenshots below. */
export function ReleaseEditor(props: ReleaseEditorProps) {
    return (
        <ReleaseDetailsProvider softwareId={props.softwareId} releaseId={props.releaseId}>
            <ReleaseEditorPage {...props}/>
        </ReleaseDetailsProvider>
    );
}

function ReleaseEditorPage({softwareId, releaseId, onDone}: ReleaseEditorProps) {
    const {t} = useTranslation();
    const release = useReleaseDetails().data;
    return (
            <WithSoftwareAndHardware
                id={softwareId}
                title={t('release.edit')}
                titleOf={(software) => t('release.editOf', {title: software.title})}
                breadcrumbsOf={release === undefined ? undefined : (software) => [
                    ...softwareBreadcrumbs(software, t('pages.software')),
                    {label: release.title ?? software.title, href: releasePath(softwareId, releaseId)},
                    {label: t('breadcrumbs.editing')},
                ]}
                actions={(software) => <DeleteAction software={software} onDone={onDone}/>}
            >
                {(software, hardware) => (
                    <ReleaseGate>
                        {(release) => (
                            <Stack gap="xl">
                                <EditReleaseProvider software={software} release={release} hardware={hardware} onSaved={onDone}>
                                    <ReleaseForm
                                        submitLabel={t('actions.save')}
                                        actions={<Button href={releasePath(softwareId, releaseId)} color="secondary">{t('actions.cancel')}</Button>}
                                    />
                                </EditReleaseProvider>
                                <ScreenshotsEditorSection softwareId={softwareId} releaseId={releaseId}/>
                            </Stack>
                        )}
                    </ReleaseGate>
                )}
            </WithSoftwareAndHardware>
    );
}

/** Deleting the release, offered next to the page title once the release is there. */
function DeleteAction({software, onDone}: {readonly software: SoftwareDetails; readonly onDone: () => void}) {
    const query = useReleaseDetails();
    return query.data === undefined ? null : <DeleteReleaseButton software={software} release={query.data} onDeleted={onDone}/>;
}

interface WithSoftwareAndHardwareProps {
    readonly id: string;
    /** The page title until the software arrives. */
    readonly title: string;
    readonly titleOf: (software: SoftwareDetails) => string;
    /** The way down to the page, once what it names is there. */
    readonly breadcrumbsOf?: ((software: SoftwareDetails) => readonly Crumb[]) | undefined;
    /** What is offered next to the page title. */
    readonly actions?: (software: SoftwareDetails) => ReactNode;
    readonly children: (software: SoftwareDetails, hardware: readonly Hardware[]) => ReactNode;
}

/** The software and the hardware of its platform, which every form of a release needs; the page is titled by the software. */
function WithSoftwareAndHardware({id, title, titleOf, breadcrumbsOf, actions, children}: WithSoftwareAndHardwareProps) {
    return (
        <SoftwareDetailsProvider key={id} id={id}>
            <SoftwareGate title={title}>
                {(software) => (
                    <PageLayout title={titleOf(software)} breadcrumbs={breadcrumbsOf?.(software)} actions={actions?.(software)}>
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
