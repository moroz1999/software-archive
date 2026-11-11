import {useNavigate, useParams} from 'react-router';
import {AUTHORS_PATH, authorPath, requireAuthorId} from '@/entities/authors';
import {AuthorEditor} from '@/widgets/author-editor';

export function AuthorEditPage() {
    const id = requireAuthorId(useParams()['id']);
    const navigate = useNavigate();
    return (
        <AuthorEditor
            id={id}
            onSaved={() => {
                void navigate(authorPath(id));
            }}
            onDeleted={() => {
                void navigate(AUTHORS_PATH);
            }}
        />
    );
}
