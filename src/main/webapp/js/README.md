JS resources
=========

## Added to appcache
Files put into the cached folder are automatically added to .appcache files. If they located in known CDN, then their local URL are replaced by a their CDN URL.

/js/cached/common: JS files are used on both front pages (index.html and content.html) and on map page (map.html).
/js/cached/front: JS files only used on front pages.
/js/cached/map: JS files only used on map page.

## Concatenated files
Any other files are not put into .appcache files. They are expected to be concatenated into other .js files, e.g. arcticweb-front.js. This concatenation is configured in HTML files. The Grunt usemin plugin is used for this.



