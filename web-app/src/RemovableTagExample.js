import {Tag} from '@shopify/polaris';
import {useEffect, useState} from 'react';
import {useLocation, useNavigate} from "react-router-dom";

export default function RemovableTagExample() {

    const [tags, setTags] = useState("")
    const [category, setCategory] = useState("")
    const [lang, setLang] = useState("")
    const useQuery = () => new URLSearchParams(useLocation().search);
    const query = useQuery();
    const navigate = useNavigate();

    useEffect(() => {

        let tags = query.get("tags");
        let category = query.get("category");
        let lang = query.get("lang");
        setTags(tags)
        setCategory(category)
        setLang(lang)
    }, [query.get("tags"), query.get("category"), query.get("lang")]);


    function handleTagRemove(type) {
        let p = query.get("p") || "";
        let kw = query.get("kw") || "";
        let t = query.get("t") || "";
        navigate(`/items?kw=${kw}&p=${p}&t=${t}&tags=${type === 'tags' ? "" : tags}&category=${type === 'category' ? "" : category}&lang=${type === 'lang' ? "" : lang}`);
    }

    return <div className={'filters'}>
        {lang && <span>
            <Tag onRemove={() => handleTagRemove('lang')}>{lang}</Tag>
        </span>}
        {category && <span>
            <Tag onRemove={() => handleTagRemove('category')}>{category}</Tag>
        </span>}
        {tags && <span>
            <Tag onRemove={() => handleTagRemove('tags')}>{tags}</Tag>
        </span>}
    </div>;
}