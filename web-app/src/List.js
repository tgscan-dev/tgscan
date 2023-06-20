import {Badge, ResourceItem, ResourceList} from '@shopify/polaris';
import React from 'react';
import {getImage} from "./utils/api";
import * as R from "ramda";


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

function chatId2link(chatId) {
    let item = JSON.parse(localStorage.getItem('roomLinks'));
    let map = item.reduce((acc, item) => {
        if (item.chatId && item.link) {
            acc[item.chatId] = item.link;
        }
        return acc;
    }, {})
    return map[chatId];
}

function chatId2name(chatId) {
    let item = JSON.parse(localStorage.getItem('roomLinks'));
    let map = item.reduce((acc, item) => {
        if (item.chatId && item.roomName) {
            acc[item.chatId] = item.roomName;
        }
        return acc;
    }, {})
    return map[chatId];
}

function Resources({items, selected}) {


    return (<ResourceList
        resourceName={{singular: 'customer', plural: 'customers'}}
        items={items}
        renderItem={(item) => {
            const {id, highlighting, desc, link, type, memberCnt, title} = item;
            if (type === 'MESSAGE') {
                //chat
                const {chatId, offset} = item;
                const {title, desc} = highlighting;
                const urls = desc.match(/(https?:\/\/[^\s]+)/g);

                return (<div className={"resource-item"}>
                        <ResourceItem
                            id={id}
                            // media={media}
                            // accessibilityLabel={`View details for ${name}`}
                        >
                            <a href={chatId2link(chatId) + "/" + offset} target={"_blank"} className={"title"}>
                                    <span className={"icon"}>
                                        {typeBadge(type)}
                                    </span>
                                <span className={"title"}
                                      dangerouslySetInnerHTML={{__html: chatId2name(chatId)}}></span>
                            </a>
                            {/*<div className={'tags'}>*/}
                            {/*    <Badge status="">*/}
                            {/*        /!*<img className={"icon"} src="/member_count.svg" alt=""/>*!/*/}
                            {/*        <span className={'cnt'}>*/}
                            {/*        {memberCnt} {memberCnt > 1 ? "MBRS" : "MBR"}*/}
                            {/*    </span>*/}
                            {/*    </Badge>*/}
                            {/*</div>*/}
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
                                {
                                    R.isNil(memberCnt) ? null :
                                        <Badge status="">
                                            {/*<img className={"icon"} src="/member_count.svg" alt=""/>*/}
                                            <span className={'cnt'}>
                                                {memberCnt} {memberCnt > 1 ? "MBRS" : "MBR"}
                                            </span>
                                        </Badge>
                                }
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