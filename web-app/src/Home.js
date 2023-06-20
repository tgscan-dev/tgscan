import React, {useContext, useEffect} from 'react';
import SearchInput from "./SearchInput";
import {Link, Page} from "@shopify/polaris";
import {useNavigate} from 'react-router-dom';
import {UserContext} from "./App";
import Footer from "./Footer";

const Home = () => {
    const navigate = useNavigate();
    const user = useContext(UserContext);

    useEffect(() => {
        const input = document.querySelector(".search-box input");
        if (input) {
            input.onkeyup = (e) => {
                let b = e.keyCode === 13 && e.target.value;
                if (b) {
                    user.loading = true;
                    navigate(`/items?kw=${e.target.value}`);
                }
            }
        }

    }, []);
    return (
        <div>
            <Page>
                <div className="container">
                    <div className={'search-box'}>
                        <div className="img">
                            <Link url={"/"}>
                                <img src="/download.svg" alt=""/>
                            </Link>
                        </div>
                        <div className="input">
                            <SearchInput></SearchInput>
                        </div>
                    </div>
                </div>
            </Page>
            <Footer></Footer>

        </div>
    );
};

export default Home;