export const getLink = (resource: any, rel: string): string | null => {
    if (!resource || !resource._links || !resource._links[rel]) {
        return null;
    }

    let href = resource._links[rel].href;

    if (href.includes('{')) {
        href = href.split('{')[0];
    }

    try {
        const urlObj = new URL(href, 'http://dummy');
        return urlObj.pathname;
    } catch (e) {
        return href.replace(/^https?:\/\/[^/]+/, '').replace(/^\/api/, '');
    }
};

export const getIdFromSelf = (resource: any): string | null => {
    try {
        let self = getLink(resource, 'self');
        if (!self) return null;

        if (self.endsWith('/')) {
            self = self.slice(0, -1);
        }

        const parts = self.split('/');
        const id = parts[parts.length - 1];
        
        return (!id || id.trim() === '') ? null : id;
    } catch (e) {
        return null;
    }
};