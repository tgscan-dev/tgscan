import React, {useCallback, useContext, useEffect, useState} from 'react';

import {Tabs} from '@shopify/polaris';
import {useLocation, useNavigate, useParams} from "react-router-dom";
import * as R from "ramda";
import {UserContext} from "./App";

function Tab({items, total, selected, setSelected}) {
    const user = useContext(UserContext);

    const params = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const useQuery = () => new URLSearchParams(location.search);
    const query = useQuery();


    const handleTabChange = useCallback(
        (selectedTabIndex) => {
            setSelected(selectedTabIndex);
            let p = query.get("p");
            if (R.isNil(p)) {
                p = 1;
            }
            const kw = user.kw;
            user.loading = true;

            navigate(`/items?kw=${kw}&p=${p}&t=${tabs[selectedTabIndex].id.toUpperCase()}`);
        },
        [],
    );

    // useEffect(() => {
    //     console.log(123)
    // }, [selected]);

    const tabs = [
        {
            id: 'All',
            content: 'All',
            accessibilityLabel: 'All',
            panelID: 'All',
        },
        {
            id: 'Group',
            content: 'Group',
            panelID: 'Group',
        },
        {
            id: 'Channel',
            content: 'Channel',
            panelID: 'Channel',
        },

        {
            id: 'Message',
            content: 'Chat',
            panelID: 'Message',
        },
        {
            id: 'Bot',
            content: 'Bot',
            panelID: 'Bot',
        },
    ];
    const [isSmallScreen, setIsSmallScreen] = useState(false);

    useEffect(() => {
        function handleResize() {
            setIsSmallScreen(window.innerWidth < 768);
        }

        window.addEventListener('resize', handleResize);
        return () => {
            window.removeEventListener('resize', handleResize);
        };
    }, []);
    return (
        <div>
            <Tabs fitted={isSmallScreen} tabs={tabs} selected={selected} onSelect={handleTabChange}>
            </Tabs>

        </div>
    );
}

export default Tab