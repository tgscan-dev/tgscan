import {FooterHelp, Link} from '@shopify/polaris';
import React from 'react';
import {useLocation} from "react-router-dom";

function Footer() {
    const useQuery = () => new URLSearchParams(useLocation().search);
    const query = useQuery();


    return (<div className={'foot'}>
            { <FooterHelp>
                © 2023 TG SCAN. &nbsp;
                <Link url="https://github.com/tg-scan/tg-scan" external={true}>
                    Github
                </Link>
            </FooterHelp>}

        </div>

    );
}

export default Footer