import {AutoSelection, Combobox, Icon, Listbox} from '@shopify/polaris';
import {SearchMinor} from '@shopify/polaris-icons';
import {useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {autocomplete} from "./utils/api";

import {useLocation, useNavigate, useParams} from 'react-router-dom';
import {UserContext} from "./App";

function SearchInput({kw}) {
    const params= useParams();
    const location = useLocation();
    const deselectedOptions = useMemo(() => [
        {value: 'NSFW', label: 'NSFW'},
        {value: 'chatGPT', label: 'chatGPT'},
        {value: 'crypto', label: 'Crypto'},
        {value: 'Telegram', label: 'Telegram'},
        {value: 'Twitter', label: 'Twitter'},
    ], [],);

    const [selectedOption, setSelectedOption] = useState();
    const [inputValue, setInputValue] = useState(kw);
    const [options, setOptions] = useState(deselectedOptions);
    const user = useContext(UserContext);



    const updateText = useCallback(async (value, e) => {
        setInputValue(value);

        if (value === '') {
            setOptions([]);
            return;
        }
        let candidates = await autocomplete(value);
        setOptions(candidates);
    }, [deselectedOptions],);

    const navigate = useNavigate();
    const updateSelection = useCallback((selected) => {
        const matchedOption = options.find((option) => {
            return option.value.match(selected);
        });

        user.loading = true;
        navigate(`/items?kw=${selected}`);
        setSelectedOption(selected);
        setInputValue((matchedOption && matchedOption.label) || '');

    }, [options],);

    const optionsMarkup = options.length > 0 ? options.map((option) => {
        const {label, value} = option;

        return (<Listbox.Option
            key={`${value}`}
            value={value}
            selected={selectedOption === value}
            accessibilityLabel={label}
        >
            {label}
        </Listbox.Option>);
    }) : null;


    return (<div>

        <Combobox
            activator={<Combobox.TextField
                prefix={<Icon source={SearchMinor}/>}
                onChange={updateText}
                label="Search telegram"
                labelHidden
                value={inputValue}
                placeholder="Search telegram"
                autoComplete="off"
            />}>

            {options.length > 0 ? (<Listbox onSelect={updateSelection}
                                            autoSelection={AutoSelection.None}>{optionsMarkup}</Listbox>) : null}
        </Combobox>

    </div>);
}

export default SearchInput