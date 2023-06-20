import '@shopify/polaris/build/esm/styles.css';
// import '@shopify/polaris/build/esm/styles.css';
import enTranslations from '@shopify/polaris/locales/en.json';
import {AppProvider} from '@shopify/polaris';
import Home from "./Home";
import Items from "./Items";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import {createContext, useContext, useEffect} from "react";
import {roomLinks} from "./utils/api";

export const UserContext = createContext({});


let init = false;

function App() {

    useEffect(() => {
        if (!init) {
            init = true;
            roomLinks().then(value => {
                localStorage.setItem('roomLinks', JSON.stringify(value));
            });
        }
    }, [])
    return (
        <UserContext.Provider value={{loading: true}}>
            <AppProvider i18n={enTranslations}>
                <BrowserRouter>
                    <Routes>
                        <Route index element={<Home/>}/>
                        <Route path="/items" element={<Items/>}/>

                    </Routes>
                </BrowserRouter>
            </AppProvider>
        </UserContext.Provider>

    );
}

export default App;
