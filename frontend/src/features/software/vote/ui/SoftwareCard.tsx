import {SoftwareCardLayout, type SoftwareCardContentProps} from '@/entities/software';
import {SoftwareCardVote} from './SoftwareCardVote';

export function SoftwareCard(props: SoftwareCardContentProps) {
    return <SoftwareCardLayout {...props} footer={<SoftwareCardVote software={props.software}/>}/>;
}
