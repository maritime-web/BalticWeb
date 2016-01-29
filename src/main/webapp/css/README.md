CSS resources
=========

## Added to appcache
Files put into the cached folder are automatically added to .appcache files. If they located in known CDN, then their local URL are replaced by a their CDN URL.

## Concatenated files
other: Any other files are not put into .appcache files. They are expected to be concatenated into other .css files, e.g. arcticweb-front.css. This concatenation is configured in HTML files. The Grunt usemin plugin is used for this.



