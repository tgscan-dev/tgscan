import {Pagination} from '@shopify/polaris';
import React, {useContext} from 'react';
import {useLocation, useNavigate, useParams} from "react-router-dom";
import {UserContext} from "./App";

function PaginationCmpt({total}) {
    const user = useContext(UserContext);

    const params = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const useQuery = () => new URLSearchParams(location.search);
    const query = useQuery();
    return (
        <div className={"pagination"}>
            <Pagination
                label={`Page ${query.get("p") ? query.get("p") : 1} of ${total}`}
                hasPrevious={query.get("p") > 1}
                onPrevious={() => {
                    const pathname = location.pathname;

                    let p = query.get("p");
                    p = p ? parseInt(p) : 2;
                    p = p > 2 ? p : 2;

                    let t = query.get("t");
                    let kw = query.get("kw");
                    t = t ? t : "";
                    user.loading = true;

                    navigate(`${pathname}?kw=${kw}&p=${p - 1}&t=${t}`);
                    window.scrollTo({ top: 0, behavior: 'smooth' });

                }}
                hasNext={query.get("p") < total}
                onNext={() => {
                    const pathname = location.pathname;

                    let p = query.get("p");
                    p = p ? parseInt(p) : 1;

                    let t = query.get("t");
                    t = t ? t : "ALL";
                    let kw = query.get("kw");
                    user.loading = true;

                    navigate(`${pathname}?kw=${kw}&p=${p + 1}&t=${t}`);
                    window.scrollTo({ top: 0, behavior: 'smooth' });

                }}
            />
        </div>
    );
}

export default PaginationCmpt