Image resources
=========

## Added to appcache
Image files under /img are with one exception (see below) automatically to the appcache files (front.appcache and map.appcache). Their location however determine which appcache files to add them to:

/img/common: Image files used on both front pages (index.html and content.html) and on map page (map.html).
/img/front: Image files only used on front pages.
/img/**/* : Image files only used on map page.

## Not added to appcache

Images may currently be unused in the application, but still saved in Git for use in the future. To avoid having them added to the appcache files automatically, these images should be saved in the folder /img/unused.
added to appcache as they will


