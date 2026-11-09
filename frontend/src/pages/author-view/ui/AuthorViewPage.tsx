import {useParams} from 'react-router';
import {requireAuthorId} from '@/entities/authors';
import {AuthorView} from '@/widgets/author-view';

export function AuthorViewPage() {
    const id = requireAuthorId(useParams()['id']);
    return <AuthorView id={id}/>;
}
