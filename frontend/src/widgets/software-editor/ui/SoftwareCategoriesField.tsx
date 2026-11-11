import {CategoriesField, CategoryTreeProvider} from '@/entities/categories';
import {useSoftwareForm} from '@/entities/software';

/** The categories of the software being filled in, chosen from the whole tree. */
export function SoftwareCategoriesField() {
    const {values, refusals, changeCategories, busy} = useSoftwareForm();
    return (
        <CategoryTreeProvider>
            <CategoriesField value={values.categories} onChange={changeCategories} disabled={busy} error={refusals.categories}/>
        </CategoryTreeProvider>
    );
}
