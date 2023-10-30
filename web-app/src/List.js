import {Badge, ResourceItem, ResourceList, Tag} from '@shopify/polaris';
import React, {useContext} from 'react';
import {getImage} from "./utils/api";
import * as R from "ramda";
import {useLocation, useNavigate} from "react-router-dom";
import {UserContext} from "./App";


function typeBadge(type) {
    switch (type) {
        case "GROUP":
            return <img src={"/group.svg"}/>
        case "CHANNEL":
            return <img src={"/channel.svg"}/>
        case "BOT":
            return <img src={"/robot.svg"}/>
        default:
            return <img src={"/chat.svg"}/>
    }
}

function chatId2link(username) {
    let item = JSON.parse(localStorage.getItem('roomLinks'));
    let map = item.reduce((acc, item) => {
        if (item.username && item.link) {
            acc[item.username] = item.link;
        }
        return acc;
    }, {})
    return map[username];
}
function capitalizeFirstLetter(string) {
    if (!string) return string;
    return string.charAt(0).toUpperCase() + string.slice(1);
}
function chatId2name(username) {
    let item = JSON.parse(localStorage.getItem('roomLinks'));
    let map = item.reduce((acc, item) => {
        if (item.username && item.roomName) {
            acc[item.username] = item.roomName;
        }
        return acc;
    }, {})
    return map[username];
}

function Resources({items, selected}) {
    const navigate = useNavigate();
    const useQuery = () => new URLSearchParams(useLocation().search);
    const query = useQuery();



    function handleTagClick(event) {
        event.preventDefault();
        event.stopPropagation();
        const tag = event.target.innerText.substring(1);
        let kw = query.get("kw");
        let category = query.get('category') ||'';
        let lang = query.get('lang') ||'';
        let p = query.get("p") || "";
        p = p ? parseInt(p) : 2;
        p = p > 2 ? p : 2;
        let t = query.get("t")||'';
        let url = `/items?kw=${kw}&tags=${tag}&p=${p - 1}&t=${t}&category=${category}&lang=${lang}`;
        navigate(url);
    }
    function formatNumber(number) {
        if (number >= 1000 && number < 1000000) {
            return (number / 1000).toFixed(1) + 'K';
        } else if (number >= 1000000 && number < 1000000000) {
            return (number / 1000000).toFixed(1) + 'M';
        } else if (number >= 1000000000) {
            return (number / 1000000000).toFixed(1) + 'B';
        } else {
            return number.toString();
        }
    }
    function handleCategoryClick(event) {
        event.preventDefault();
        event.stopPropagation();
        const category = event.target.innerText;
        let kw = query.get("kw");
        let tag = query.get('tags') ||'';
        let lang = query.get('lang') ||'';

        let p = query.get("p") || "";
        p = p ? parseInt(p) : 2;
        p = p > 2 ? p : 2;
        let t = query.get("t") ||'';
        let url = `/items?kw=${kw}&tags=${tag}&p=${p - 1}&t=${t}&category=${category}&lang=${lang}`;
        navigate(url);
    }

    function handleLangClick(event){
        event.preventDefault();
        event.stopPropagation();
        const lang = event.target.innerText;
        let kw = query.get("kw");
        let tag = query.get('tags') ||'';
        let category = query.get('category') ||'';
        let p = query.get("p");
        p = p ? parseInt(p) : 2;
        p = p > 2 ? p : 2;
        let t = query.get("t") || '';
        let url = `/items?kw=${kw}&tags=${tag}&p=${p - 1}&t=${t}&category=${category}&lang=${lang}`;
        navigate(url);
    }
    let category0 = query.get("category");
    let tags0 = query.get("tags");
    let lang0 = query.get("lang");
    return (<ResourceList
        resourceName={{singular: 'customer', plural: 'customers'}}
        items={items}
        renderItem={(item) => {
            const {id, highlighting, desc, link, type, memberCnt, title, lang, tags, category} = item;
            if (type === 'MESSAGE') {
                //chat
                const {username, offset} = item;
                const {title, desc} = highlighting;
                const urls = desc.match(/(https?:\/\/[^\s]+)/g);

                return (<div className={"resource-item"}>
                        <ResourceItem
                            id={id}
                        >
                            <a href={chatId2link(username) + "/" + offset} target={"_blank"} className={"title"}>
                                    <span className={"icon"}>
                                        {typeBadge(type)}
                                    </span>
                                <span className={"title"}
                                      dangerouslySetInnerHTML={{__html: chatId2name(username)}}></span>
                            </a>

                            <div className={"highlight-body"} onClick={event => event.stopPropagation()}
                                 dangerouslySetInnerHTML={{__html: desc.replace(/\n/g, '<br>')}}></div>


                        </ResourceItem>
                    </div>

                );
            } else {
                const {name, jhiDesc} = highlighting;
                const split = link.split("/");
                const short = split[split.length - 1];
                const media = <span id={'avatar'} onClick={() => {
                    open(link, "_blank")
                }}>

                <img src={getImage(short)} alt={short}/>
            </span>;

                return (<div className={"resource-item"}>
                        <ResourceItem
                            id={id}
                            media={media}
                            accessibilityLabel={`View details for ${name}`}
                        >
                            <a href={link} target={"_blank"} className={"title"}>
                                    <span className={"icon"}>
                                        {typeBadge(type)}
                                    </span>
                                <span className={"title"} dangerouslySetInnerHTML={{__html: name}}></span>
                            </a>
                            <div className={'tags'}>
                                <div  url="#">
                                    Language:
                                    <span className={'tag-item'} style={{'color':`${lang===lang0?'red':''}`}} onClick={handleLangClick}>
                                           <em/>
                                    {capitalizeFirstLetter(lang)}
                                </span>
                                    </div>
                                <div  url="#">
                                    Category: <em/>
                                    <span className={'tag-item'} style={{'color':`${category0===capitalizeFirstLetter(category)?'red':''}`}}  onClick={handleCategoryClick}
                                    >
                                    {/*    upcase first of category*/}
                                    {capitalizeFirstLetter(category)}
                                    </span>
                                </div>
                                {
                                    (type === 'BOT' || R.isNil(memberCnt)) ? null :
                                        <div >
                                            Members: <em></em>
                                            <span className={'bold'}>{formatNumber(memberCnt)}</span>
                                        </div>
                                }
                                <div  url="#">
                                    Tags: <em/>{tags.map(tag0 => <span className={`tag-item`} style={{'color':`${tag0===tags0?'red':''}`}} onClick={handleTagClick}>#{tag0}</span>)}
                                </div>


                            </div>
                            <div className={"highlight-body"} onClick={event => event.stopPropagation()}
                                 dangerouslySetInnerHTML={{__html: jhiDesc}}></div>


                        </ResourceItem>
                    </div>

                );
            }

        }}
    />);
}

export default Resources