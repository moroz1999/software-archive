import {useNavigate} from 'react-router';
import {authorPath} from '@/entities/authors';
import {AuthorCreator} from '@/widgets/author-editor';

export function AuthorNewPage() {
    const navigate = useNavigate();
    return (
        <AuthorCreator onCreated={(id) => {
            void navigate(authorPath(id));
        }}/>
    );
}
