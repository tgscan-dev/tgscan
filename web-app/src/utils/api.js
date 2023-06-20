import * as R from "ramda";

let BACKEND = `http://localhost:9000/api`;
let UI = `http://localhost:9000/api/search/image`;
const isProduction = process.env.NODE_ENV === 'production';
console.log(process.env.NODE_ENV)
if (isProduction) {
    BACKEND = `/api`;
    UI = `/images`;
}

export async function autocomplete(kw) {
    const response = await fetch(`${BACKEND}/search/autocomplete?kw=${kw}`);
    return (await response.json()).map((item) => ({value: item, label: item}));

}

export async function search(kw, page, type) {
    if (!kw) {
        return {total: 0, doc: []}
    }
    if (R.isNil(page) || page < 1) {
        page = 1;
    }
    if (R.isNil(type) || R.isEmpty(type)) {
        type = '';
    }
    const response = await fetch(`${BACKEND}/search/query?kw=${kw}&p=${page ? page : 1}&t=${type}`);
    return response.json();
}

export function getImage(id) {
    return `${UI}/${id}.jpg`;
}

export async function roomLinks() {
    const response = await fetch(`${BACKEND}/search/roomLinks`);
    return response.json();
}