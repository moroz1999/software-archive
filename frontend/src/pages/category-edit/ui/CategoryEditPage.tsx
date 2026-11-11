import {useNavigate, useParams} from 'react-router';
import {CATEGORIES_PATH, requireCategoryId} from '@/entities/categories';
import {CategoryEditor} from '@/widgets/category-editor';

export function CategoryEditPage() {
    const id = requireCategoryId(useParams()['id']);
    const navigate = useNavigate();
    function backToTree() {
        void navigate(CATEGORIES_PATH);
    }
    return <CategoryEditor id={id} onSaved={backToTree} onDeleted={backToTree}/>;
}
