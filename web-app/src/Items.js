import React, {useContext, useEffect, useState} from 'react';
import SearchInput from "./SearchInput";
import {Divider, Link, Spinner} from "@shopify/polaris";
import Tab from "./Tab";
import {useLocation, useNavigate} from "react-router-dom";
import {search} from "./utils/api";
import pDebounce from 'p-debounce';
import Resources from "./List";
import PaginationCmpt from "./PaginationCmpt";
import {UserContext} from "./App";
import {toast, ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.css';
import RemovableTagExample from "./RemovableTagExample";


function type2selected(type) {
    switch (type) {
        case "GROUP":
            return 1;
        case "CHANNEL":
            return 2;
        case "BOT":
            return 4;
        case "MESSAGE":
            return 3;
        case "ALL":
            return 0;
        default:
            return 0;
    }
}

const Items = () => {
    const user = useContext(UserContext);

    const navigate = useNavigate();
    const useQuery = () => new URLSearchParams(useLocation().search);
    const query = useQuery();
    const [selected, setSelected] = useState(0);
    const [items, setItems] = useState([]);
    const [total, setTotal] = useState(0);
    const debounceSearch = pDebounce(search, 200);
    const debounceToastError = pDebounce(toast.error, 200);

    // const [loading, setLoading] = useState(true);
    useEffect(() => {
        const input = document.querySelector(".items .input input");
        if (input) {
            input.onkeyup = (e) => {
                let value = e.target.value;
                if (e.keyCode === 13 && value) {
                    let kw = query.get("kw");
                    if (kw.trim() === value.trim()) {
                        return
                    }
                    user.loading = true;
                    navigate(`/items?kw=${value}`);
                    // close auto complete
                    window.document.body.click();
                }
            };
        }
        const page = query.get("p");
        let type = query.get("t");
        let kw = query.get("kw");

        user.kw = kw;
        setSelected(type2selected(type));


        let tags = query.get("tags");
        let category = query.get("category");
        let lang = query.get("lang");
        debounceSearch(kw, category, tags, lang, page, type).then((res) => {
            if (!res.doc) {
                return
            }
            setItems(res.doc);
            setTotal(res.totalPage);
            user.loading = false;
        }).catch((e) => {
            // debugger
            console.log(e);
            let _ = debounceToastError('Operation failed, please try again later.', {
                position: "bottom-right",
                autoClose: 5000,
                hideProgressBar: false,
                closeOnClick: true,
                pauseOnHover: true,
                draggable: true,
                progress: undefined,
                theme: "light",
            });
            user.loading = false;
        })
    }, [query.get("kw"), query.get("p"), query.get("t"), query.get("tags"), query.get("category"),query.get("lang")]);

    return (
        <div className="items">
            <ToastContainer/>

            <div className="head">
                <div className="img">
                    <Link url={"/"}>
                        <img src="/download.svg" alt=""/>
                    </Link>
                </div>
                <div className="input">
                    <SearchInput kw={query.get("kw")}></SearchInput>
                </div>

            </div>
            <div className="tabs">
                <Tab items={items} total={total} selected={selected} setSelected={setSelected}></Tab>
            </div>
            <Divider/>
            <RemovableTagExample></RemovableTagExample>
            <div className="results">
                {user.loading && <div id={"loading"}><Spinner accessibilityLabel="Spinner" size="large"/></div>}
                {!user.loading && <div>
                    <div className="resources">
                        <Resources items={items} selected={selected}></Resources>
                    </div>
                    <PaginationCmpt total={total}></PaginationCmpt></div>}
            </div>
        </div>
    );
};

export default Items;